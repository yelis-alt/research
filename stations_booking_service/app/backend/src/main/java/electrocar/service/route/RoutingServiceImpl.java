package electrocar.service.route;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import electrocar.dto.common.LocationDTO;
import electrocar.dto.enums.PlugType;
import electrocar.dto.route.*;
import electrocar.dto.station.FilterStationDTO;
import electrocar.dto.entity.Station;
import electrocar.mapper.SimpleBeanRowMapper;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Component
@RequiredArgsConstructor
public class RoutingServiceImpl implements RoutingService {
    private static final Logger logger = LoggerFactory.getLogger(RoutingServiceImpl.class);
    private static final  String ROUTE_REQUEST =
            "https://api.openrouteservice.org/v2/directions/driving-car";
    private static final String API_JSON = "api.json";
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final OpenRouteServiceRequestDTO openRouteServiceRequest =
            new OpenRouteServiceRequestDTO(new ArrayList<>(), false, "km", false, "shortest", false);
    private static final Gson gson = new Gson();
    private static final String COST = "cost";
    private static final String DURATION = "duration";
    private static final String DISTANCE = "distance";
    private static final String TRIP_DURATION = "tripDuration";
    private static final String ROUTES = "routes";
    private static final String SUMMARY = "summary";
    private static final String START_POINT = "start point";
    private static final String FINISH_POINT = "finish point";
    private static final double startNodeReachDuration = 0.0;
    private static final double minEnergyDcCharge = 12.4;
    private static final double accOptCoef = 0.8;
    private static final double accChargeTether = 0.2;
    private static final int speed = 45;
    private static final int price = 15;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleBeanRowMapper<Station> rowMapper =
            new SimpleBeanRowMapper<>(Station.class);
    private final HttpHeaders routeHeaders = new HttpHeaders();
    private final HttpHeaders fastApiHeaders = new HttpHeaders();

    @Override
    public List<Station> getFilteredStations(FilterStationDTO filterStationDTO) {

        String sql = """
                     SELECT * FROM electrocar.station
                     WHERE (plug=:plug OR plug IS NULL) AND
                           (plug_type IN (:plugType) OR plug_type IS NULL) AND
                           ((power>=:fromPower AND power<=:toPower) OR power IS NULL) AND
                           ((price>=:fromPrice AND price<=:toPrice) OR price IS NULL)
                     """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("plug", filterStationDTO.getPlug().toString());
        params.addValue("plugType", filterStationDTO.getPlugType().stream()
                                                              .map(Enum::toString).toList());
        params.addValue("fromPower", filterStationDTO.getFromPower());
        params.addValue("toPower", filterStationDTO.getToPower());
        params.addValue("fromPrice", filterStationDTO.getFromPrice());
        params.addValue("toPrice", filterStationDTO.getToPrice());

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public List<RouteOutputDTO> getRoute(RouteRequestDTO routeRequestDTO) {
        double accMax = routeRequestDTO.getAccMax();
        double accOpt = roundToTwoDecimals(accOptCoef*accMax);
        double spendOpt = routeRequestDTO.getSpendOpt();
        double temp = routeRequestDTO.getTemperature();
        double accBegin = accMax * routeRequestDTO.getAccLevel()/100;

        Station startPoint = new Station();
        startPoint.setLongitude(routeRequestDTO.getStartCoords().getLongitude());
        startPoint.setLatitude(routeRequestDTO.getStartCoords().getLatitude());
        Station finishPoint = new Station();
        finishPoint.setLongitude(routeRequestDTO.getFinishCoords().getLongitude());
        finishPoint.setLatitude(routeRequestDTO.getFinishCoords().getLatitude());

        Map<String, Double> directRouteMap =
                getEdgeCostAndDuration(startPoint, finishPoint, spendOpt, accBegin,
                                       accMax, accOpt, temp, true);
        if (!directRouteMap.isEmpty()) {
            List<RouteOutputDTO> routeNodesList = new ArrayList<>();
            routeNodesList.add(new RouteOutputDTO(startPoint, startNodeReachDuration));
            routeNodesList.add(new RouteOutputDTO(finishPoint, directRouteMap.get(DURATION)));

            return routeNodesList;
        }

        Map<Integer, List<Map<Integer, Map<String, Double>>>> adjacencyMatrix =
                getAdjacencyMatrix(routeRequestDTO, startPoint, finishPoint,
                                   accBegin, accOpt, spendOpt, accMax, temp);

        return new ArrayList<>();
    }

    public Map<Integer, List<Map<Integer, Map<String, Double>>>>
           getAdjacencyMatrix(RouteRequestDTO routeRequestDTO,
                              Station startPoint, Station finishPoint,
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

        startPoint.setId(0);
        nodesList.add(startPoint);
        nodesList = nodesList.stream()
                .sorted(Comparator.comparing(Station::getId))
                .collect(Collectors.toList());
        nodesList.add(finishPoint);

        Map<Integer, List<Map<Integer, Map<String, Double>>>> matrix = new HashMap<>();
        for (Station nodeStart: nodesList) {
            Integer nodeStartId = nodeStart.getId();
            if (nodeStartId == null) {
                break;
            }

            double accStart;
            if (nodeStartId == 0) {
                accStart = accBegin;
            } else {
                accStart = accOpt;
            }

            for (Station nodeFinish: nodesList) {
                Integer nodeFinishId = nodeFinish.getId();
                if (!nodeStartId.equals(nodeFinishId) &&
                    !Objects.equals(nodeFinishId, 0) &&
                    !(nodeStartId == 0 && nodeFinishId == null)) {
                    boolean directRouteFlag = nodeFinishId == null;
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
                    directRouteMap.put(DURATION, timeDist);
                    return directRouteMap;
                } else {
                    if (accFinish < accOpt) {
                        double power = nodeFinish.getPower();
                        double timeWait = roundToTwoDecimals((double) ThreadLocalRandom.current()
                                .nextInt(1, 5 + 1) /60);

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
                        double duration = timeDist + timeWait + timeCharge;
                        double cost = roundToTwoDecimals(spendAct*price*(dist + speed*timeDist) +
                                                         spendAct*price*speed*(timeWait + timeCharge) +
                                                         price*(accOpt - accFinish));

                        Map<String, Double> edgeMap = new HashMap<>();
                        edgeMap.put(COST, cost);
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
        System.out.println((nodeStart.getId() == null ? "null" : nodeStart.getId().toString()) + " || " +
                (nodeFinish.getId() == null ? "null" : nodeFinish.getId().toString()));
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
            Map<String, String> routePointMap =
                    getRoutePointsMap(directRouteFlag, nodeStart, nodeFinish);

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

        double deltaX = firstX - secondX;
        double deltaY = firstY - secondY;

        return Math.sqrt(sq(deltaX) + sq(deltaY));
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
            if (nodeStart.getId() == null) {
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

    public double sq(double x) {
        return x*x;
    }

    public double roundToTwoDecimals(double val) {
        val = val*100;
        val = Math.round(val);

        return val /100;
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
