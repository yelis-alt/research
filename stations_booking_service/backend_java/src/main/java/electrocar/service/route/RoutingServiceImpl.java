package electrocar.service.route;

import static java.lang.Math.round;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import electrocar.dao.StationDao;
import electrocar.dto.common.DurationDTO;
import electrocar.dto.common.LocationDTO;
import electrocar.dto.entity.Station;
import electrocar.dto.evolution.GenerationDTO;
import electrocar.dto.route.*;
import electrocar.dto.station.FilterStationDTO;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingServiceImpl implements RoutingService {
    private static final String COST = "cost";
    private static final String DURATION = "duration";
    private static final String DISTANCE = "distance";
    private static final String TRIP_DURATION = "tripDuration";
    private static final String ROUTES = "routes";
    private static final String SUMMARY = "summary";
    private static final String CHARGE_DURATION = "chargeDuration";
    private static final String START_POINT = "startCoords point";
    private static final String FINISH_POINT = "finishCoords point";
    private static final String DECIMAL_POINT = "\\.";
    private static final double ACC_OPT_COEFFICIENT = 0.8;
    private static final double ACC_CHARGE_TETHER = 0.2;
    private static final int SPEED = 45;
    private static final int PRICE = 15;
    private static final int GEN_SUCCESS_REP = 20;
    private static final int GEN_REP = 1000;

    private final OpenRouteServiceRequestDTO openRouteServiceRequest;
    private final RestTemplate restTemplate;
    private final Gson gson;
    private final Random rand;
    private final StationDao stationDao;

    @Value("${openRouteService.request.url}")
    private String routeRequestUrl;

    @Value("${openRouteService.request.api}")
    private String api;

    @Value("${python.service.origin}")
    private String pythonServiceOrigin;

    private HttpHeaders routeHeaders;
    private HttpHeaders fastApiHeaders;

    @Override
    public List<Station> getFilteredStationsList(FilterStationDTO filterStation) {
        return stationDao.getFilteredStations(filterStation);
    }

    @Override
    public List<RouteNodeDTO> getRoute(RouteRequestDTO routeRequest) {
        // define route parameters
        double accMax = routeRequest.getAccMax();
        double accOpt = roundToTwoDecimals(ACC_OPT_COEFFICIENT * accMax);
        double spendOpt = routeRequest.getSpendOpt();
        double temp = routeRequest.getTemperature();
        double accBegin = accMax * routeRequest.getAccLevel() / 100;

        // try to build direct route between startCoords point and finishCoords point
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
                getEdgeCostAndDuration(startPoint, finishPoint, spendOpt, accBegin, accMax, accOpt, temp, true);
        if (!directRouteMap.isEmpty()) {
            List<RouteNodeDTO> routeNodesList = new ArrayList<>();
            routeNodesList.add(new RouteNodeDTO(startPoint, 0.0, 0.0, new DurationDTO(0, 0), new DurationDTO(0, 0)));

            double reachDuration = directRouteMap.get(TRIP_DURATION);
            int hours = getHoursFromDuration(reachDuration);
            int minutes = getMinutesFromReachDuration(reachDuration);
            routeNodesList.add(new RouteNodeDTO(
                    finishPoint,
                    directRouteMap.get(DISTANCE),
                    directRouteMap.get(COST),
                    new DurationDTO(0, 0),
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
        // return getRouteWithEvolution(adjacencyMatrix, routeRequest);
    }

    public Map<Integer, List<Map<Integer, Map<String, Double>>>> getAdjacencyMatrix(
            RouteRequestDTO routeRequestDTO,
            double accBegin,
            double accOpt,
            double spendOpt,
            double accMax,
            double temp) {
        List<Station> stationsList = routeRequestDTO.getFilteredStationsList();
        List<Station> nodesListRaw =
                stationsList.stream().filter(Station::getStatus).toList();

        LocationDTO startCoords = routeRequestDTO.getStartCoords();
        LocationDTO finishCoords = routeRequestDTO.getFinishCoords();
        double diameter = getEuclideanDist(List.of(startCoords, finishCoords));
        LocationDTO midpoint = getMidpoint(startCoords, finishCoords);
        List<Station> nodesList = getStationsListFilteredByDist(nodesListRaw, midpoint, diameter);
        if (nodesList.isEmpty()) {
            return new HashMap<>();
        }

        Map<Integer, List<Map<Integer, Map<String, Double>>>> matrix = new HashMap<>();
        for (Station nodeStart : nodesList) {
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

            for (Station nodeFinish : nodesList) {
                int nodeFinishId = nodeFinish.getId();
                if (!nodeStartId.equals(nodeFinishId)
                        && !Objects.equals(nodeFinishId, 0)
                        && !(nodeStartId == 0 && nodeFinishId == Integer.MAX_VALUE)) {
                    boolean directRouteFlag = nodeFinishId == Integer.MAX_VALUE;
                    Map<String, Double> edgeMap = getEdgeCostAndDuration(
                            nodeStart, nodeFinish, spendOpt, accStart, accMax, accOpt, temp, directRouteFlag);
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

    Map<String, Double> getEdgeCostAndDuration(
            Station nodeStart,
            Station nodeFinish,
            double spendOpt,
            double accStart,
            double accMax,
            double accOpt,
            double temp,
            boolean directRouteFlag) {
        try {
            Map<String, Double> routeParams = getRouteParams(nodeStart, nodeFinish);
            double dist = routeParams.get(DISTANCE);
            double timeDist = routeParams.get(TRIP_DURATION);
            double spendAct = (0.005 * spendOpt * (0.1 * sq(temp) - 4 * temp + 240)) / 100;
            double accFinish = roundToTwoDecimals(accStart - spendAct * dist);

            if (accFinish >= 0) {
                if (directRouteFlag) {
                    Map<String, Double> directRouteMap = new HashMap<>();
                    directRouteMap.put(COST, roundToTwoDecimals(spendAct * PRICE * (dist + SPEED * timeDist)));
                    directRouteMap.put(DISTANCE, dist);
                    directRouteMap.put(TRIP_DURATION, timeDist);
                    directRouteMap.put(CHARGE_DURATION, 0.0);

                    return directRouteMap;
                } else {
                    if (accFinish < accOpt) {
                        double power = nodeFinish.getPower();
                        double timeWait = roundToTwoDecimals((double) (rand.nextInt(5) + 1) / 60);

                        double timeCharge = 0;
                        switch (nodeFinish.getPlugType()) {
                            case AC: {
                                if (accFinish < ACC_CHARGE_TETHER * accMax) {
                                    timeCharge += (ACC_CHARGE_TETHER * accMax - accFinish) / (0.5 * power);
                                    timeCharge += (accOpt - ACC_CHARGE_TETHER * accMax) / power;
                                } else {
                                    timeCharge += (accOpt - accFinish) / power;
                                }

                                break;
                            }

                            case DC: {
                                double accDiff = accOpt - accFinish;
                                timeCharge += getTimeWaitFromRegressionModel(temp, accDiff);

                                break;
                            }
                        }
                        timeCharge = roundToTwoDecimals(timeCharge);
                        double cost = roundToTwoDecimals(spendAct * PRICE * (dist + SPEED * timeDist)
                                + spendAct * PRICE * SPEED * (timeWait + timeCharge)
                                + PRICE * (accOpt - accFinish));

                        Map<String, Double> edgeMap = new HashMap<>();
                        edgeMap.put(COST, cost);
                        edgeMap.put(DISTANCE, dist);
                        edgeMap.put(TRIP_DURATION, timeDist);
                        edgeMap.put(CHARGE_DURATION, roundToTwoDecimals(timeCharge + timeWait));

                        return edgeMap;
                    } else {

                        return new HashMap<>();
                    }
                }
            } else {

                return new HashMap<>();
            }
        } catch (Exception E) {
            Map<String, String> routePointMap = getRoutePointsMap(nodeStart, nodeFinish);

            log.error("Unable to build the route between nodes '"
                    + routePointMap.get(START_POINT)
                    + "' and '"
                    + routePointMap.get(FINISH_POINT)
                    + "' due to "
                    + E);

            return new HashMap<>();
        }
    }

    public Map<String, Double> getRouteParams(Station nodeStart, Station nodeFinish) throws InterruptedException {
        Map<String, String> routePointMap = getRoutePointsMap(nodeStart, nodeFinish);
        log.info(routePointMap.get(START_POINT) + " --> " + routePointMap.get(FINISH_POINT));
        TimeUnit.SECONDS.sleep(2);
        double startLong = nodeStart.getLongitude();
        double startLat = nodeStart.getLatitude();
        double finishLong = nodeFinish.getLongitude();
        double finishLat = nodeFinish.getLatitude();
        openRouteServiceRequest.setCoordinates(new ArrayList<>());
        openRouteServiceRequest.getCoordinates().add(List.of(startLat, startLong));
        openRouteServiceRequest.getCoordinates().add(List.of(finishLat, finishLong));

        ResponseEntity<Object> responseEntity = restTemplate.exchange(
                routeRequestUrl,
                HttpMethod.POST,
                new HttpEntity<>(openRouteServiceRequest, routeHeaders),
                Object.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            JsonObject jsonResponse = gson.toJsonTree(responseEntity.getBody()).getAsJsonObject();
            JsonArray interMap = (JsonArray) jsonResponse.get(ROUTES);
            JsonObject preFinalMap =
                    interMap.get(0).getAsJsonObject().get(SUMMARY).getAsJsonObject();

            Map<String, Double> resMap = new HashMap<>();
            resMap.put(DISTANCE, roundToTwoDecimals(preFinalMap.get(DISTANCE).getAsDouble()));
            resMap.put(
                    TRIP_DURATION, roundToTwoDecimals(preFinalMap.get(DURATION).getAsDouble() / 3600));

            return resMap;
        } else {
            log.error("Unable to determine route parameters between nodes '"
                    + routePointMap.get(START_POINT)
                    + "' and '"
                    + routePointMap.get(FINISH_POINT)
                    + "' due to "
                    + responseEntity);

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

    public LocationDTO getMidpoint(LocationDTO firstPoint, LocationDTO secondPoint) {
        double firstX = firstPoint.getLongitude();
        double firstY = firstPoint.getLatitude();
        double secondX = secondPoint.getLongitude();
        double secondY = secondPoint.getLatitude();

        return new LocationDTO((firstX + secondX) / 2, (firstY + secondY) / 2);
    }

    public List<Station> getStationsListFilteredByDist(
            List<Station> stationsList, LocationDTO midpoint, double diameter) {
        double radiusExtended = diameter / 2 * 1.2;

        List<Station> nodesFiltered = new ArrayList<>();
        for (Station station : stationsList) {
            LocationDTO stationCoords = new LocationDTO(station.getLongitude(), station.getLatitude());
            double dist = getEuclideanDist(List.of(midpoint, stationCoords));

            if (dist <= radiusExtended) {
                nodesFiltered.add(station);
            }
        }

        return nodesFiltered;
    }

    public double getTimeWaitFromRegressionModel(double temperature, double accDiff) {
        DcChargeDurationRequestDTO request = new DcChargeDurationRequestDTO(List.of(accDiff), List.of(temperature));

        ResponseEntity<DcChargeDurationOutputDTO> responseEntity = restTemplate.exchange(
                pythonServiceOrigin + "routing/getDcChargeDuration",
                HttpMethod.POST,
                new HttpEntity<>(request, fastApiHeaders),
                DcChargeDurationOutputDTO.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return Objects.requireNonNull(responseEntity.getBody()).getTimeCharge();
        } else {
            throw new InternalError("Unable to determine DC charge session duration with parameters: '"
                    + temperature
                    + "°C' and '"
                    + accDiff
                    + "kWh'");
        }
    }

    public Map<String, String> getRoutePointsMap(Station nodeStart, Station nodeFinish) {
        String addressStart;
        if (nodeStart.getId() == 0) {
            addressStart = START_POINT;
        } else {
            addressStart = nodeStart.getAddress();
        }

        String addressFinish;
        if (nodeFinish.getId() == Integer.MAX_VALUE) {
            addressFinish = FINISH_POINT;
        } else {
            addressFinish = nodeFinish.getAddress();
        }

        Map<String, String> resMap = new HashMap<>();
        resMap.put(START_POINT, addressStart);
        resMap.put(FINISH_POINT, addressFinish);

        return resMap;
    }

    public List<RouteNodeDTO> getRouteWithDijkstra(
            Map<Integer, List<Map<Integer, Map<String, Double>>>> adjacencyMatrix, RouteRequestDTO routeRequest) {

        Map<Integer, Double> routeMap = new HashMap<>();
        Map<Integer, Integer> connectMap = new HashMap<>();
        List<Integer> queue = new ArrayList<>();
        for (Map.Entry<Integer, List<Map<Integer, Map<String, Double>>>> entry : adjacencyMatrix.entrySet()) {
            int entryId = entry.getKey();
            if (entryId == 0) {
                routeMap.put(entryId, 0.0);
            } else {
                routeMap.put(entryId, Double.MAX_VALUE);
            }
            connectMap.put(entryId, null);
            queue.add(entryId);
        }

        List<Integer> pathIdsList = new ArrayList<>();
        routeMap.put(Integer.MAX_VALUE, Double.MAX_VALUE);
        while (!queue.isEmpty()) {
            int keyMin = queue.get(0);
            log.info("Nodes left: " + queue.size());

            double valMin = routeMap.get(keyMin);

            for (int pos = 0; pos < queue.size(); pos++) {
                if (routeMap.get(queue.get(pos)) < valMin) {
                    keyMin = queue.get(pos);
                    valMin = routeMap.get(keyMin);
                }

                int currentNodeId = keyMin;
                queue.removeIf(id -> Objects.equals(id, currentNodeId));

                for (Map<Integer, Map<String, Double>> nextNodeMap : adjacencyMatrix.get(currentNodeId)) {
                    int nextNodeMapId = nextNodeMap.keySet().iterator().next();
                    double routeCost = nextNodeMap.values().iterator().next().get(COST) + routeMap.get(currentNodeId);
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
            Map<Integer, List<Map<Integer, Map<String, Double>>>> adjacencyMatrix, RouteRequestDTO routeRequest) {

        List<Integer> adjacencyNodesList = new ArrayList<>();
        for (Map.Entry<Integer, List<Map<Integer, Map<String, Double>>>> node : adjacencyMatrix.entrySet()) {
            adjacencyNodesList.add(node.getKey());
            adjacencyNodesList.addAll(node.getValue().stream()
                    .map(n -> n.keySet().iterator().next())
                    .toList());
        }
        adjacencyNodesList = new ArrayList<>(new HashSet<>(adjacencyNodesList));

        GenerationDTO parent = new GenerationDTO(new HashMap<>(), new ArrayList<>());
        for (Integer id : adjacencyNodesList) {
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

        while (success < GEN_SUCCESS_REP) {
            rep++;
            log.info("Generation " + rep + "out of " + GEN_REP + "; Successful DNAs: " + success);

            if (rep > GEN_REP) {
                if (success == 0) {
                    return new ArrayList<>();
                }
            }

            int randOperator = rand.nextInt(4);
            GenerationDTO child = new GenerationDTO();
            switch (randOperator) {
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
            for (Map.Entry<Integer, Integer> entry : child.getDnaMap().entrySet()) {
                if (entry.getValue() == 1) {
                    pathIdsList.add(entry.getKey());
                }
            }

            double routeCost = 0;
            boolean failureFlag = false;
            pathIdsList = pathIdsList.stream().sorted().toList();
            Set<Integer> currentNodeIdsSet = adjacencyMatrix.keySet();
            for (int i = 0; i < pathIdsList.size() - 1; i++) {
                int currentNodeId = pathIdsList.get(i);

                if (currentNodeIdsSet.contains(currentNodeId)) {
                    List<Map<Integer, Map<String, Double>>> nextNodesList = adjacencyMatrix.get(currentNodeId);
                    int nextNodeId = pathIdsList.get(i + 1);
                    List<Map<Integer, Map<String, Double>>> nextNodesListFilter = nextNodesList.stream()
                            .filter(node ->
                                    Objects.equals(node.keySet().iterator().next(), nextNodeId))
                            .toList();

                    if (nextNodesListFilter.isEmpty()) {
                        failureFlag = true;
                        break;
                    } else {
                        routeCost += nextNodesListFilter
                                .get(0)
                                .values()
                                .iterator()
                                .next()
                                .get(COST);
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
        log.info("Generation " + rep + "out of " + GEN_REP + "; Successful DNAs: " + success);

        return getRouteNodesDromIdsList(adjacencyMatrix, bestPathIdsList, routeRequest);
    }

    public GenerationDTO getMutation(GenerationDTO generation, List<Integer> geneIdsList) {
        int randomGene = rand.nextInt(geneIdsList.size() - 2) + 1;
        int mutatedGeneId = geneIdsList.get(randomGene);
        generation.getDnaMap().put(mutatedGeneId, 1 - generation.getDnaMap().get(mutatedGeneId));

        return generation;
    }

    public GenerationDTO getCrossover(GenerationDTO generation, List<Integer> geneIdsList) {
        List<Map<Integer, Integer>> parentDnasList = generation.getParentsList();
        if (parentDnasList.size() < 3) {
            GenerationDTO mutant = getMutation(generation, geneIdsList);
            mutant.getParentsList().add(mutant.getDnaMap());

            return mutant;
        }

        Map<Integer, Integer> parent1 = parentDnasList.get(rand.nextInt(parentDnasList.size() - 1));
        Map<Integer, Integer> parent2 = parentDnasList.get(rand.nextInt(parentDnasList.size() - 1));

        for (int geneId : geneIdsList.subList(1, geneIdsList.size() - 1)) {
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
        double chargeDurationCumm = 0.0;
        int previousId = 0;
        List<RouteNodeDTO> routeNodesList = new ArrayList<>();
        for (Station station : routeRequest.getFilteredStationsList()) {
            int stationId = station.getId();
            if (pathIdsList.contains(stationId)) {
                RouteNodeDTO routeNode = new RouteNodeDTO();
                routeNode.setRouteNode(station);

                if (stationId != 0) {
                    Map<Integer, Map<String, Double>> edgeMap = adjacencyMatrix.get(previousId).stream()
                            .filter(stMap -> stMap.keySet().iterator().next() == stationId)
                            .toList()
                            .get(0);
                    double chargeDuration = edgeMap.values().iterator().next().get(CHARGE_DURATION);
                    distanceCumm += roundToTwoDecimals(
                            edgeMap.values().iterator().next().get(DISTANCE));
                    costCumm += roundToTwoDecimals(
                            edgeMap.values().iterator().next().get(COST));
                    reachDurationCumm += edgeMap.values().iterator().next().get(TRIP_DURATION);
                    reachDurationCumm += chargeDurationCumm;
                    chargeDurationCumm += chargeDuration;

                    Integer hoursCharge = getHoursFromDuration(chargeDuration);
                    Integer minutesCharge = getMinutesFromReachDuration(chargeDuration);
                    Integer hours = getHoursFromDuration(reachDurationCumm);
                    Integer minutes = getMinutesFromReachDuration(reachDurationCumm);

                    routeNode.setDistance(distanceCumm);
                    routeNode.setCost(costCumm);
                    routeNode.setChargeDuration(new DurationDTO(hoursCharge, minutesCharge));
                    routeNode.setReachDuration(new DurationDTO(hours, minutes));
                } else {
                    routeNode.setDistance(0.0);
                    routeNode.setCost(0.0);
                    routeNode.setChargeDuration(new DurationDTO(0, 0));
                    routeNode.setReachDuration(new DurationDTO(0, 0));
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
        afterDecimalPartString = afterDecimalPartString.substring(0, 2);
        double minutesDouble = round(Double.parseDouble(afterDecimalPartString) / 100 * 60);

        return Integer.parseInt(String.valueOf(minutesDouble).split(DECIMAL_POINT)[0]);
    }

    public double sq(double x) {
        return x * x;
    }

    public double roundToTwoDecimals(double val) {
        val = val * 100;
        val = round(val);

        return val / 100;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initHeaders() {
        routeHeaders = new HttpHeaders();
        routeHeaders.add("Authorization", api);
        routeHeaders.add(
                "Accept", "application/json, application/geo+json, " + "application/gpx+xml, img/png; charset=utf-8");
        routeHeaders.add("Content-Type", "application/json; charset=utf-8");

        fastApiHeaders = new HttpHeaders();
        fastApiHeaders.add("Content-Type", "application/json");
        fastApiHeaders.add("Accept", "application/json");
    }
}
