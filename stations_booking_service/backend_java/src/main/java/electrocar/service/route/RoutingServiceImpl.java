package electrocar.service.route;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import electrocar.dao.StationDao;
import electrocar.dto.common.DurationDTO;
import electrocar.dto.common.LocationDTO;
import electrocar.dto.evolution.GenerationDTO;
import electrocar.dto.route.*;
import electrocar.dto.station.FilterStationDTO;
import electrocar.dto.entity.Station;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Math.round;

@Service
@RequiredArgsConstructor
public class RoutingServiceImpl implements RoutingService {
    private static final Logger logger = LoggerFactory.getLogger(RoutingServiceImpl.class);
    private static final String ROUTE_REQUEST = "https://api.openrouteservice.org/v2/directions/driving-car";
    private static final String API_JSON = "api.json";
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final OpenRouteServiceRequestDTO openRouteServiceRequest =
            new OpenRouteServiceRequestDTO(new ArrayList<>(), false, "km", false, "shortest", false);
    private static final Gson gson = new Gson();
    private static final Random rand = new Random();
    private static final String COST = "cost";
    private static final String DURATION = "duration";
    private static final String DISTANCE = "distance";
    private static final String TRIP_DURATION = "tripDuration";
    private static final String ROUTES = "routes";
    private static final String SUMMARY = "summary";
    private static final String CHARGE_DURATION = "chargeDuration";
    private static final String START_POINT = "start point";
    private static final String FINISH_POINT = "finish point";
    private static final String DECIMAL_POINT = "\\.";
    private static final double startNodeValDouble = 0.0;
    private static final int startNodeValInt = 0;
    private static final double minEnergyDcCharge = 12.4;
    private static final double accOptCoef = 0.8;
    private static final double accChargeTether = 0.2;
    private static final int speed = 45;
    private static final int price = 15;
    private static final int genSuccessRep = 5;
    private static final int genRep = 1000;

    private final HttpHeaders routeHeaders = new HttpHeaders();
    private final HttpHeaders fastApiHeaders = new HttpHeaders();
    private final StationDao stationDao;

    @Override
    public List<Station> getFilteredStations(FilterStationDTO filterStation) {
        return stationDao.getFilteredStations(filterStation);
    }

    @Override
    public List<RouteNodeDTO> getRoute(RouteRequestDTO routeRequest) {
        // define route parameters
        double accMax = routeRequest.getAccMax();
        double accOpt = roundToTwoDecimals(accOptCoef*accMax);
        double spendOpt = routeRequest.getSpendOpt();
        double temp = routeRequest.getTemperature();
        double accBegin = accMax * routeRequest.getAccLevel()/100;

        // try to build direct route between start point and finish point
        Station startPoint = new Station();
        startPoint.setId(0);
        startPoint.setStatus(true);
        startPoint.setLongitude(routeRequest.getStartCoords().getLongitude());
        startPoint.setLatitude(routeRequest.getStartCoords().getLatitude());
        Station finishPoint = new Station();
        finishPoint.setId(Integer.MAX_VALUE);
        finishPoint.setStatus(true);
        finishPoint.setLongitude(routeRequest.getFinishCoords().getLongitude());
        finishPoint.setLatitude(routeRequest.getFinishCoords().getLatitude());

        Map<String, Double> directRouteMap =
                getEdgeCostAndDuration(startPoint, finishPoint, spendOpt, accBegin,
                                       accMax, accOpt, temp, true);
        if (!directRouteMap.isEmpty()) {
            List<RouteNodeDTO> routeNodesList = new ArrayList<>();
            routeNodesList.add(new RouteNodeDTO(
                    startPoint, startNodeValDouble, startNodeValDouble,
                    new DurationDTO(startNodeValInt,startNodeValInt),
                    new DurationDTO(startNodeValInt,startNodeValInt)));

            double reachDuration = directRouteMap.get(DURATION);
            int hours = getHoursFromDuration(reachDuration);
            int minutes = getMinutesFromReachDuration(reachDuration);
            routeNodesList.add(new RouteNodeDTO(
                    finishPoint, directRouteMap.get(DISTANCE),
                    directRouteMap.get(COST),
                    new DurationDTO(startNodeValInt,startNodeValInt),
                    new DurationDTO(hours, minutes)));

            return routeNodesList;
        }

        // build adjacency matrix
        List<Station> routeRequestStaionsList = routeRequest.getFilteredStationsList();
        routeRequestStaionsList.add(startPoint);
        routeRequestStaionsList.add(finishPoint);
        routeRequestStaionsList = routeRequestStaionsList.stream()
                .sorted(Comparator.comparing(Station::getId))
                .collect(Collectors.toList());
        routeRequest.setFilteredStationsList(routeRequestStaionsList);

        Map<Integer, List<Map<Integer, Map<String, Double>>>> adjacencyMatrix =
                getAdjacencyMatrix(routeRequest, accBegin, accOpt, spendOpt, accMax, temp);

        // apply Dijkstra's algorithm for adjacency matrix
        return getRouteWithDijkstra(adjacencyMatrix, routeRequest);

        // apply genetic algorithms for adjacency matrix
        //return getRouteWithEvolution(adjacencyMatrix, routeRequest);
    }

    public Map<Integer, List<Map<Integer, Map<String, Double>>>>
           getAdjacencyMatrix(RouteRequestDTO routeRequestDTO,
                              double accBegin, double accOpt, double spendOpt,
                              double accMax, double temp) {
        List<Station> stationsList = routeRequestDTO.getFilteredStationsList();
        List<Station> nodesListRaw = stationsList.stream()
                                                 .filter(Station::getStatus)
                                                 .toList();

        LocationDTO startCoords = routeRequestDTO.getStartCoords();
        LocationDTO finishCoords = routeRequestDTO.getFinishCoords();
        double diameter = getEuclideanDist(List.of(startCoords, finishCoords));
        LocationDTO midpoint = getMidpoint(startCoords, finishCoords);
        List<Station> nodesList = getStationsListFilteredByDist(nodesListRaw,
                                                                midpoint,
                                                                diameter);
        if (nodesList.isEmpty()) {
            return new HashMap<>();
        }

        Map<Integer, List<Map<Integer, Map<String, Double>>>> matrix = new HashMap<>();
        for (Station nodeStart: nodesList) {
            Integer nodeStartId = nodeStart.getId();
            if (nodeStartId == Integer.MAX_VALUE) {
                break;
            }

            double accStart;
            if (nodeStartId == 0) {
                accStart = accBegin;
            } else {
                accStart = accOpt;
            }

            for (Station nodeFinish: nodesList) {
                int nodeFinishId = nodeFinish.getId();
                if (!nodeStartId.equals(nodeFinishId) &&
                    !Objects.equals(nodeFinishId, 0) &&
                    !(nodeStartId == 0 && nodeFinishId == Integer.MAX_VALUE)) {
                    boolean directRouteFlag = nodeFinishId == Integer.MAX_VALUE;
                    Map<String, Double> edgeMap =
                            getEdgeCostAndDuration(nodeStart, nodeFinish, spendOpt, accStart,
                                                   accMax, accOpt, temp, directRouteFlag);
                    if (!edgeMap.isEmpty()) {
                        if (!matrix.containsKey(nodeStartId)) {
                            matrix.put(nodeStartId, new ArrayList<>());
                        }

                        Map<Integer, Map<String, Double>> finishNodeMap = new HashMap<>();
                        finishNodeMap.put(nodeFinishId, edgeMap);
                        matrix.get(nodeStartId).add(finishNodeMap);
                    }
                }
            }
        }

        return matrix;
    }

    Map<String, Double> getEdgeCostAndDuration(Station nodeStart, Station nodeFinish,
                                               double spendOpt, double accStart, double accMax,
                                               double accOpt, double temp, boolean directRouteFlag) {
        try {
            Map<String, Double> routeParams = getRouteParams(nodeStart, nodeFinish, directRouteFlag);
            double dist = routeParams.get(DISTANCE);
            double timeDist = routeParams.get(TRIP_DURATION);
            double spendAct = (0.005*spendOpt*(0.1*sq(temp) - 4*temp + 240))/100;
            double accFinish = roundToTwoDecimals(accStart  - spendAct*dist);

            if (accFinish >= 0) {
                if (directRouteFlag) {
                    Map<String, Double> directRouteMap = new HashMap<>();
                    directRouteMap.put(COST, roundToTwoDecimals(spendAct*price*(dist + speed*timeDist)));
                    directRouteMap.put(DISTANCE, dist);
                    directRouteMap.put(DURATION, timeDist);

                    return directRouteMap;
                } else {
                    if (accFinish < accOpt) {
                        double power = nodeFinish.getPower();
                        double timeWait = (double) (rand.nextInt(5) + 1)/60;

                        double timeCharge = 0;
                        switch (nodeFinish.getPlugType()) {
                            case AC: {
                                if (accFinish < accChargeTether*accMax) {
                                    timeCharge += (accChargeTether*accMax - accFinish)/(0.5*power);
                                    timeCharge += (accOpt - accChargeTether*accMax)/power;
                                } else {
                                    timeCharge += (accOpt - accFinish)/power;
                                }

                                break;
                            }

                            case DC: {
                                double accDiff = accOpt - accFinish;
                                if (accDiff >= minEnergyDcCharge) {
                                    timeCharge += getTimeWaitFromRegressionModel(temp, accDiff);
                                } else {
                                    Map<String, String> routePointMap =
                                            getRoutePointsMap(false, nodeStart, nodeFinish);

                                    logger.error("Unable to build the route between nodes '"+
                                            routePointMap.get(START_POINT) + "' and '" +
                                            routePointMap.get(FINISH_POINT) + "' due to small amount of energy " +
                                            "to be replenished by DC charging station");

                                    return new HashMap<>();
                                }

                                break;
                            }
                        }
                        timeCharge = roundToTwoDecimals(timeCharge);
                        double duration = roundToTwoDecimals(timeDist + timeWait + timeCharge);
                        double cost = roundToTwoDecimals(spendAct*price*(dist + speed*timeDist) +
                                                         spendAct*price*speed*(timeWait + timeCharge) +
                                                         price*(accOpt - accFinish));

                        Map<String, Double> edgeMap = new HashMap<>();
                        edgeMap.put(COST, cost);
                        edgeMap.put(DISTANCE, dist);
                        edgeMap.put(CHARGE_DURATION, timeCharge);
                        edgeMap.put(DURATION, duration);

                        return edgeMap;
                    } else {

                        return new HashMap<>();
                    }
                }
            } else {

                return new HashMap<>();
            }
        } catch (Exception E) {
            Map<String, String> routePointMap =
                    getRoutePointsMap(directRouteFlag, nodeStart, nodeFinish);

            logger.error("Unable to build the route between nodes '"+
                         routePointMap.get(START_POINT) + "' and '" +
                         routePointMap.get(FINISH_POINT) + "' due to " + E);

            return new HashMap<>();
        }
    }

    public Map<String, Double> getRouteParams(Station nodeStart,
                                              Station nodeFinish,
                                              boolean directRouteFlag) throws InterruptedException {
        Map<String, String> routePointMap =
                getRoutePointsMap(directRouteFlag, nodeStart, nodeFinish);
        logger.info(routePointMap.get(START_POINT) + " --> " + routePointMap.get(FINISH_POINT));
        TimeUnit.SECONDS.sleep(2);
        double startLong = nodeStart.getLongitude();
        double startLat = nodeStart.getLatitude();
        double finishLong = nodeFinish.getLongitude();
        double finishLat = nodeFinish.getLatitude();
        openRouteServiceRequest.setCoordinates(new ArrayList<>());
        openRouteServiceRequest.getCoordinates().add(List.of(startLat, startLong));
        openRouteServiceRequest.getCoordinates().add(List.of(finishLat, finishLong));

        ResponseEntity<Object> responseEntity =
                restTemplate.exchange(
                        ROUTE_REQUEST,
                        HttpMethod.POST,
                        new HttpEntity<>(openRouteServiceRequest, routeHeaders),
                        Object.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            JsonObject jsonResponse = gson.toJsonTree(
                    responseEntity.getBody()).getAsJsonObject();
            JsonArray interMap = (JsonArray) jsonResponse.get(ROUTES);
            JsonObject preFinalMap = interMap.get(0).getAsJsonObject()
                    .get(SUMMARY).getAsJsonObject();

            Map<String, Double> resMap = new HashMap<>();
            resMap.put(DISTANCE, roundToTwoDecimals(preFinalMap.get(DISTANCE).getAsDouble()));
            resMap.put(TRIP_DURATION, roundToTwoDecimals(preFinalMap.get(DURATION).getAsDouble() / 3600));

            return resMap;
        } else {
            logger.error("Unable to determine route parameters between nodes '" +
                    routePointMap.get(START_POINT) + "' and '" +
                    routePointMap.get(FINISH_POINT) + "' due to " + responseEntity);

            return new HashMap<>();
        }
    }

    public double getEuclideanDist(List<LocationDTO> coordsList) {
        double firstX = coordsList.get(0).getLongitude();
        double firstY = coordsList.get(0).getLatitude();
        double secondX = coordsList.get(1).getLongitude();
        double secondY = coordsList.get(1).getLatitude();

        return Math.sqrt(sq(firstX - secondX) + sq(firstY - secondY));
    }

    public LocationDTO getMidpoint(LocationDTO firstPoint,
                                   LocationDTO secondPoint) {
        double firstX = firstPoint.getLongitude();
        double firstY = firstPoint.getLatitude();
        double secondX = secondPoint.getLongitude();
        double secondY = secondPoint.getLatitude();

        return new LocationDTO((firstX + secondX)/2,
                               (firstY + secondY)/2);
    }

    public List<Station> getStationsListFilteredByDist(List<Station> stationsList,
                                                       LocationDTO midpoint,
                                                       double diameter) {
        double radiusExtended = diameter/2 * 1.2;

        List<Station> nodesFiltered = new ArrayList<>();
        for (Station station: stationsList) {
            LocationDTO stationCoords = new LocationDTO(station.getLongitude(),
                                                        station.getLatitude());
            double dist = getEuclideanDist(List.of(midpoint, stationCoords));

            if (dist <= radiusExtended) {
                nodesFiltered.add(station);
            }
        }

        return nodesFiltered;
    }

    public double getTimeWaitFromRegressionModel(double temperature, double accDiff) {
        DcChargeDurationRequestDTO request =
                new DcChargeDurationRequestDTO(List.of(accDiff), List.of(temperature));

        ResponseEntity<DcChargeDurationOutputDTO> responseEntity =
                restTemplate.exchange(
                        "http://127.0.0.1:8000/routing/getDcChargeDuration",
                        HttpMethod.POST,
                        new HttpEntity<>(request, fastApiHeaders),
                        DcChargeDurationOutputDTO.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return Objects.requireNonNull(
                    responseEntity.getBody()).getTimeCharge();
        } else {
            throw new InternalError(
                    "Unable to determine DC charge session duration with parameters: '" +
                    temperature + "°C' and '" + accDiff + "kWh'");
        }
    }

    public Map<String, String> getRoutePointsMap(boolean directRouteFlag,
                                                 Station nodeStart,
                                                 Station nodeFinish) {
        String addressStart;
        String addressFinish;
        if (directRouteFlag) {
            if (nodeStart.getId() == 0) {
                addressStart = START_POINT;
            } else {
                addressStart = nodeStart.getAddress();
            }
            addressFinish = FINISH_POINT;
        } else {
            addressStart = nodeStart.getAddress();
            addressFinish = nodeFinish.getAddress();
        }

        Map<String, String> resMap = new HashMap<>();
        resMap.put(START_POINT, addressStart);
        resMap.put(FINISH_POINT, addressFinish);

        return resMap;
    }

    public List<RouteNodeDTO> getRouteWithDijkstra(
            Map<Integer, List<Map<Integer, Map<String, Double>>>> adjacencyMatrix,
            RouteRequestDTO routeRequest) {

        Map<Integer, Double> routeMap = new HashMap<>();
        Map<Integer, Integer> connectMap = new HashMap<>();
        List<Integer> queue = new ArrayList<>();
        for (Map.Entry<Integer, List<Map<Integer, Map<String, Double>>>>
                entry: adjacencyMatrix.entrySet()) {
            int entryId = entry.getKey();
            if (entryId == 0){
                routeMap.put(entryId, startNodeValDouble);
            } else {
                routeMap.put(entryId, Double.MAX_VALUE);
            }
            connectMap.put(entryId,  null);
            queue.add(entryId);
        }

        List<Integer> pathIdsList = new ArrayList<>();
        routeMap.put(Integer.MAX_VALUE, Double.MAX_VALUE);
        while (!queue.isEmpty()) {
           int keyMin = queue.get(0);
           logger.info("Nodes left: " + queue.size());

           double valMin = routeMap.get(keyMin);

           for (int pos = 0; pos < queue.size(); pos++) {
               if (routeMap.get(queue.get(pos)) < valMin) {
                   keyMin = queue.get(pos);
                   valMin = routeMap.get(keyMin);
               }

               int currentNodeId = keyMin;
               queue.removeIf(id -> Objects.equals(id, currentNodeId));

               for (Map<Integer, Map<String, Double>> nextNodeMap:
                       adjacencyMatrix.get(currentNodeId)) {
                   int nextNodeMapId = nextNodeMap.keySet().iterator().next();
                   double routeCost = nextNodeMap.values().iterator().next().get(COST) +
                           routeMap.get(currentNodeId);
                   if (routeMap.get(nextNodeMapId) > routeCost) {
                       routeMap.put(nextNodeMapId, routeCost);
                       connectMap.put(nextNodeMapId, currentNodeId);
                   }
               }
           }
        }

        Integer finishId = Integer.MAX_VALUE;
        pathIdsList.add(finishId);
        while (true) {
            finishId = connectMap.get(finishId);
            if (finishId == null) {
                break;
            }

            pathIdsList.add(finishId);
        }
        Collections.reverse(pathIdsList);

        return getRouteNodesDromIdsList(adjacencyMatrix, pathIdsList, routeRequest);
    }

    public List<RouteNodeDTO> getRouteWithEvolution(
            Map<Integer, List<Map<Integer, Map<String, Double>>>> adjacencyMatrix,
            RouteRequestDTO routeRequest) {

        List<Integer> adjacencyNodesList = new ArrayList<>();
        for (Map.Entry<Integer, List<Map<Integer, Map<String, Double>>>> node:
                adjacencyMatrix.entrySet()) {
            adjacencyNodesList.add(node.getKey());
            adjacencyNodesList.addAll(
                    node.getValue().stream().map(n -> n.keySet().iterator().next()).toList());
        }
        adjacencyNodesList = new ArrayList<>(new HashSet<>(adjacencyNodesList));

        GenerationDTO parent =
                new GenerationDTO(new HashMap<>(), new ArrayList<>());
        for (Integer id: adjacencyNodesList) {
            if (id == 0 || id == Integer.MAX_VALUE) {
                parent.getDnaMap().put(id, 1);
            } else {
                parent.getDnaMap().put(id, 0);
            }
        }

        int rep = 0;
        int success = 0;
        double minCost = Double.MAX_VALUE;
        List<Integer> bestPathIdsList = new ArrayList<>();
        List<Integer> geneIdsList =
                parent.getDnaMap().keySet().stream().sorted().toList();

        while (success < genSuccessRep) {
            rep ++;
            logger.info("Generation " + rep + "out of " + genRep +
                    "; Successful DNAs: " + success);

            if (rep > genRep) {
                if (success == 0) {
                    return new ArrayList<>();
                }
            }

            int randOperator = rand.nextInt(4);
            GenerationDTO child = new GenerationDTO();
            switch(randOperator) {
                case 0: {
                    child = getMutation(parent, geneIdsList);
                    break;
                }
                case 1: {
                    child = getCrossover(parent, geneIdsList);
                    break;
                }
                case 2: {
                    child = getMutation(parent, geneIdsList);
                    child = getCrossover(child, geneIdsList);
                    break;
                }
                case 3: {
                    child = getCrossover(parent, geneIdsList);
                    child = getMutation(child, geneIdsList);
                    break;
                }
            }

            List<Integer> pathIdsList = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry: child.getDnaMap().entrySet()) {
                if (entry.getValue() == 1) {
                    pathIdsList.add(entry.getKey());
                }
            }

            double routeCost = 0;
            boolean failureFlag = false;
            pathIdsList = pathIdsList.stream().sorted().toList();
            Set<Integer> currentNodeIdsSet = adjacencyMatrix.keySet();
            for (int i = 0; i < pathIdsList.size()-1; i++) {
                int currentNodeId = pathIdsList.get(i);

                if (currentNodeIdsSet.contains(currentNodeId)) {
                    List<Map<Integer, Map<String, Double>>> nextNodesList =
                            adjacencyMatrix.get(currentNodeId);
                    int nextNodeId = pathIdsList.get(i + 1);
                    List<Map<Integer, Map<String, Double>>> nextNodesListFilter =
                            nextNodesList.stream().filter(node ->
                                    Objects.equals(node.keySet().iterator().next(), nextNodeId)).toList();

                    if (nextNodesListFilter.isEmpty()) {
                        failureFlag = true;
                        break;
                    } else {
                        routeCost += nextNodesListFilter.get(0).values().iterator().next().get(COST);
                    }
                } else {
                    failureFlag = true;
                    break;
                }

            }

            if (!failureFlag) {
                success++;

                if (routeCost < minCost) {
                    minCost = routeCost;
                    bestPathIdsList = pathIdsList;
                }
            }

            parent = child;
        }
        logger.info("Generation " + rep + "out of " + genRep +
                "; Successful DNAs: " + success);

        return getRouteNodesDromIdsList(adjacencyMatrix, bestPathIdsList, routeRequest);
    }

    public GenerationDTO getMutation(GenerationDTO generation, List<Integer> geneIdsList) {
        int randomGene = rand.nextInt(geneIdsList.size() - 2) + 1;
        int mutatedGeneId = geneIdsList.get(randomGene);
        generation.getDnaMap().put(mutatedGeneId,
                1 - generation.getDnaMap().get(mutatedGeneId));

        return generation;
    }

    public GenerationDTO getCrossover(GenerationDTO generation, List<Integer> geneIdsList) {
        List<Map<Integer, Integer>> parentDnasList = generation.getParentsList();
        if (parentDnasList.size() < 3) {
            GenerationDTO mutant =  getMutation(generation, geneIdsList);
            mutant.getParentsList().add(mutant.getDnaMap());

            return mutant;
        }

        Map<Integer,Integer> parent1 = parentDnasList.get(rand.nextInt(parentDnasList.size() - 1));
        Map<Integer,Integer> parent2 = parentDnasList.get(rand.nextInt(parentDnasList.size() - 1));

        for (int geneId: geneIdsList.subList(1, geneIdsList.size()-1)) {
            int childGene = parent1.get(geneId) + parent2.get(geneId);
            if (childGene == 2) {
                generation.getDnaMap().put(geneId, rand.nextInt(2));
            } else {
                generation.getDnaMap().put(geneId, childGene);
            }
        }

        generation.getParentsList().add(generation.getDnaMap());

        return generation;
    }

    public List<RouteNodeDTO> getRouteNodesDromIdsList(
            Map<Integer, List<Map<Integer, Map<String, Double>>>> adjacencyMatrix,
            List<Integer> pathIdsList,
            RouteRequestDTO routeRequest) {
        double reachDurationCumm = 0.0;
        double distanceCumm = 0.0;
        double costCumm = 0.0;
        int previousId = 0;
        List<RouteNodeDTO> routeNodesList = new ArrayList<>();
        for (Station station: routeRequest.getFilteredStationsList()) {
            int stationId = station.getId();
            if (pathIdsList.contains(stationId)) {
                RouteNodeDTO routeNode = new RouteNodeDTO();
                routeNode.setRouteNode(station);

                if (stationId != 0) {
                    Map<Integer, Map<String, Double>> edgeMap = adjacencyMatrix.get(previousId).stream()
                            .filter(stMap -> stMap.keySet().iterator().next() == stationId).toList().get(0);
                    distanceCumm += edgeMap.values().iterator().next().get(DISTANCE);
                    costCumm += roundToTwoDecimals(edgeMap.values().iterator().next().get(COST));
                    reachDurationCumm += edgeMap.values().iterator().next().get(DURATION);
                    double chargeDuration = edgeMap.values().iterator().next().get(CHARGE_DURATION);

                    Integer hoursCharge = getHoursFromDuration(chargeDuration);
                    Integer minutesCharge = getMinutesFromReachDuration(chargeDuration);
                    Integer hours = getHoursFromDuration(reachDurationCumm);
                    Integer minutes = getMinutesFromReachDuration(reachDurationCumm);

                    routeNode.setDistance(distanceCumm);
                    routeNode.setCost(costCumm);
                    routeNode.setChargeDuration(new DurationDTO(hoursCharge, minutesCharge));
                    routeNode.setReachDuration(new DurationDTO(hours, minutes));
                } else {
                    routeNode.setDistance(startNodeValDouble);
                    routeNode.setCost(startNodeValDouble);
                    routeNode.setChargeDuration(new DurationDTO(startNodeValInt, startNodeValInt));
                    routeNode.setReachDuration(new DurationDTO(startNodeValInt, startNodeValInt));
                }

                previousId = stationId;
                routeNodesList.add(routeNode);
            }
        }

        return routeNodesList;
    }

    public int getHoursFromDuration(double reachDuration) {
        String beforeDecimalPartString = String.valueOf(reachDuration).split(DECIMAL_POINT)[0];

        return Integer.parseInt(beforeDecimalPartString);
    }

    public int getMinutesFromReachDuration(double reachDuration) {
        String afterDecimalPartString = String.valueOf(reachDuration).split(DECIMAL_POINT)[1];
        if (afterDecimalPartString.length() == 1) {
            afterDecimalPartString += "0";
        }
        afterDecimalPartString = afterDecimalPartString.substring(0,2);
        double minutesDouble = round(Double.parseDouble(afterDecimalPartString)/100*60);

        return Integer.parseInt(String.valueOf(minutesDouble).split(DECIMAL_POINT)[0]);
    }

    public double sq(double x) {
        return x*x;
    }

    public double roundToTwoDecimals(double val) {
        val = val*100;
        val = round(val);

        return val/100;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initHeaders() throws FileNotFoundException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(API_JSON));
        JsonObject jsonApi = gson.fromJson(bufferedReader, JsonObject.class);
        String api = String.valueOf(jsonApi.get("open_route")).replace("\"", "");

        routeHeaders.add("Authorization", api);
        routeHeaders.add("Accept", "application/json, application/geo+json, " +
                "application/gpx+xml, img/png; charset=utf-8");
        routeHeaders.add("Content-Type", "application/json; charset=utf-8");

        fastApiHeaders.add("Content-Type", "application/json");
        fastApiHeaders.add("Accept", "application/json");
    }
}
