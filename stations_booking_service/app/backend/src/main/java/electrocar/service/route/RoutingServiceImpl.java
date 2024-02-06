package electrocar.service.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import electrocar.dto.common.LocationDTO;
import electrocar.dto.route.OpenRouteServiceRequestDTO;
import electrocar.dto.route.RouteRequestDTO;
import electrocar.dto.station.FilterStationDTO;
import electrocar.dto.route.RouteOutputDTO;
import electrocar.dto.entity.Station;
import electrocar.mapper.SimpleBeanRowMapper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Component
@RequiredArgsConstructor
public class RoutingServiceImpl implements RoutingService {
    private static final Logger logger = LoggerFactory.getLogger(RoutingServiceImpl.class);
    private final static String ROUTE_REQUEST =
            "https://api.openrouteservice.org/v2/directions/driving-car";
    private final static String ROUTE_API =
            "5b3ce3597851110001cf6248eb51a5f80f97435cbfa27a3f642d9c19";
    private final static ObjectMapper mapper = new ObjectMapper();
    private final static Client client = ClientBuilder.newClient();
    private static final OpenRouteServiceRequestDTO openRouteServiceRequest =
            new OpenRouteServiceRequestDTO(new ArrayList<>(), false, "km", false, "shortest", false);
    private final static String COST = "cost";
    private final static String DURATION = "duration";
    private final static String DISTANCE = "distance";
    private final static String TRIP_DURATION = "tripDuration";
    private final static double startNodeReachDuration = 0.0;
    private final static double accOptCoef = 0.8;
    private final static double accChargeTether = 0.2;
    private final static int speed = 45;
    private final static int price = 15;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleBeanRowMapper<Station> rowMapper =
            new SimpleBeanRowMapper<>(Station.class);

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
        double accOpt = accOptCoef*accMax;
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
                    !(nodeStartId == 0 && nodeFinishId == null)) {
                    Map<String, Double> edgeMap =
                            getEdgeCostAndDuration(nodeStart, nodeFinish, spendOpt, accStart,
                                                   accMax, accOpt, temp, false);
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
            Map<String, Double> routeParams = getRouteParams(nodeStart, nodeFinish);
            double dist = routeParams.get("distance");
            double timeDist = routeParams.get("duration");
            double spendAct = (0.005*spendOpt*(0.1*sq(temp) - 4*temp + 240))/100;
            double accFinish = accStart  - spendAct*dist;

            if (accFinish >= 0) {
                if (directRouteFlag) {
                    return Map.of(DURATION, timeDist);
                } else {
                    if (accFinish < accOpt) {
                        double power = nodeFinish.getPower();
                        double timeWait = (double) ThreadLocalRandom.current()
                                .nextInt(1, 5 + 1) /60;

                        double timeCharge = 0;
                        switch (nodeFinish.getPlugType()) {
                            case AC: {
                                if (accFinish < accChargeTether*accMax) {
                                    timeCharge += (accChargeTether*accMax - accFinish)/(0.5*power);
                                    timeCharge += (accOpt - accChargeTether*accMax)/power;
                                } else {
                                    timeCharge += (accOpt - accFinish)/power;
                                }
                            }
                            case DC: {
                                timeCharge += getTimeWaitFromRegressionModel(temp, accOpt - accFinish);
                            }
                        }

                        double duration = timeDist + timeWait + timeCharge;
                        double cost = spendAct*price*(dist + speed*timeDist) +
                                spendAct*price*speed*(timeWait + timeCharge) +
                                price*(accOpt - accFinish);

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
            logger.error("Unable to build the route between nodes '"+
                         nodeStart.getAddress() + "' and '" +
                         nodeFinish.getAddress() + "' due to " + E);

            return new HashMap<>();
        }
    }

    public Map<String, Double> getRouteParams(Station nodeStart, Station nodeFinish)
            throws JsonProcessingException {
        double startLong = nodeStart.getLongitude();
        double startLat = nodeStart.getLatitude();
        double finishLong = nodeFinish.getLongitude();
        double finishLat = nodeFinish.getLatitude();
        openRouteServiceRequest.setCoordinates(new ArrayList<>());
        openRouteServiceRequest.getCoordinates().add(List.of(startLat, startLong));
        openRouteServiceRequest.getCoordinates().add(List.of(finishLat, finishLong));

        String jsonString = mapper.writeValueAsString(openRouteServiceRequest)
                                  .replace("suppressWarnings", "suppress_warnings");
        Entity<String> payload = Entity.json(jsonString);
        Response response = client.target(ROUTE_REQUEST)
                .request()
                .header("Authorization", ROUTE_API)
                .header("Accept", "application/json, application/geo+json, " +
                                     "application/gpx+xml, img/png; charset=utf-8")
                .header("Content-Type", "application/json; charset=utf-8")
                .post(payload);

        assert response.getStatus() == 200;
        JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));
        JSONObject interMap = (JSONObject) jsonResponse.getJSONArray("routes").get(0);
        JSONObject preFinalMap = interMap.getJSONObject("summary");

        Map<String, Double> resMap = new HashMap<>();
        resMap.put(DISTANCE, Double.parseDouble(preFinalMap.get("distance").toString()));
        resMap.put(TRIP_DURATION, Double.parseDouble(preFinalMap.get("duration").toString())/3600);

        return resMap;
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
        double radiusExtended = diameter/2 * 1.5;

        List<Station> nodesFiltered = new ArrayList<>();
        for (Station station: stationsList) {
            LocationDTO stationCoords = new LocationDTO(station.getLongitude(),
                                                        station.getLatitude());
            //double dist = getEuclideanDist(List.of(midpoint, stationCoords));
            double dist = 0.01;

            if (dist <= radiusExtended) {
                nodesFiltered.add(station);
            }
        }

        return nodesFiltered;
    }

    public double getTimeWaitFromRegressionModel(double temp, double accDiff) {
        return 0.0;
    }

    public double sq(double x) {
        return x*x;
    }
}
