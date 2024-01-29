package electrocar.service.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import electrocar.dto.common.LocationDTO;
import electrocar.dto.route.OpenRouteServiceRequestDTO;
import electrocar.dto.station.FilterStationDTO;
import electrocar.dto.route.RouteOutputDTO;
import electrocar.dto.route.RouteRequestDTO;
import electrocar.dto.entity.Station;
import electrocar.mapper.SimpleBeanRowMapper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Component
@RequiredArgsConstructor
public class RoutingServiceImpl implements RoutingService {
    private final static String ROUTE_REQUEST =
            "https://api.openrouteservice.org/v2/directions/driving-car";
    private final static String ROUTE_API =
            "5b3ce3597851110001cf6248eb51a5f80f97435cbfa27a3f642d9c19";
    private final static ObjectMapper mapper = new ObjectMapper();
    private final static Client client = ClientBuilder.newClient();
    private static OpenRouteServiceRequestDTO openRouteServiceRequest =
            new OpenRouteServiceRequestDTO(new ArrayList<>(), false, "km", false,
                                           "shortest", false);
    private final static Integer SPEED = 45;
    private final static Integer PRICE = 15;

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
    public RouteOutputDTO getRoute(RouteRequestDTO routeRequestDTO) {
        Station startPoint = new Station();
        startPoint.setId(-2);
        startPoint.setLongitude(routeRequestDTO.getStartCoords().getLongitude());
        startPoint.setLatitude(routeRequestDTO.getStartCoords().getLatitude());
        Station finishPoint = new Station();
        finishPoint.setId(-1);
        finishPoint.setLongitude(routeRequestDTO.getFinishCoords().getLongitude());
        finishPoint.setLatitude(routeRequestDTO.getFinishCoords().getLatitude());
        Map<Integer, Map<Integer, Map<String, Double>>> d = getAdjacencyMatrix(routeRequestDTO, startPoint, finishPoint);


        return null;
    }


    public Map<Integer, Map<Integer, Map<String, Double>>>
           getAdjacencyMatrix(RouteRequestDTO routeRequestDTO,
                              Station startPoint, Station finishPoint) {
        Double accMax = routeRequestDTO.getAccMax();
        Double spendOpt = routeRequestDTO.getSpendOpt();
        double accStart = accMax * routeRequestDTO.getAccLevel()/100;
        double accMaxAct = accMax * 0.8;

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
            return null;
        }

        startPoint.setId(0);
        nodesList.add(startPoint);
        nodesList = nodesList.stream()
                .sorted(Comparator.comparing(Station::getId))
                .collect(Collectors.toList());
        nodesList.add(finishPoint);

        int nodesListLen = nodesList.size();
        Map<Integer, Map<Integer, Map<String, Double>>> matrix  = new HashMap<>();
        for (int i = 0; i < (nodesListLen - 1); i++) {
            double accAct;
            if (i == 0) {
                accAct = accStart;
            } else {
                accAct = accMaxAct;
            }

            for (int j = 0; j < nodesListLen; j++) {
                if (i != j &&
                    !(i == 0 && j == (nodesListLen - 1))) {
                    Map<String, Double> edgeMap =
                            getEdgeCostAndDuration(nodesList.get(i), nodesList.get(j),
                                                   spendOpt, accAct);
                    if (!edgeMap.isEmpty()) {
                        if (!matrix.containsKey(i)) {
                            matrix.put(i, new HashMap<>());
                        }
                        matrix.get(i).put(j, edgeMap);
                    }
                }
            }
        }

        return matrix;
    }

    Map<String, Double> getEdgeCostAndDuration(Station nodeStart, Station nodeFinish,
                                               Double spendOpt, Double accAct) {
        try {
            Map<String, Double> routeParams = getRouteParams(nodeStart, nodeFinish);

            return  new HashMap<>();
        } catch (Exception E) {
            return new HashMap<>();
        }
    }

    public Map<String, Double> getRouteParams(Station nodeStart, Station nodeFinish) throws JsonProcessingException {
        Double startLong = nodeStart.getLongitude();
        Double startLat = nodeStart.getLatitude();
        Double finishLong = nodeFinish.getLongitude();
        Double finishLat = nodeFinish.getLatitude();
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
        resMap.put("distance", Double.parseDouble(preFinalMap.get("distance").toString()));
        resMap.put("duration", Double.parseDouble(preFinalMap.get("duration").toString())/3600);

        return resMap;
    }

    public double getEuclideanDist(List<LocationDTO> coordsList) {
        Double firstX = coordsList.get(0).getLongitude();
        Double firstY = coordsList.get(0).getLatitude();
        Double secondX = coordsList.get(1).getLongitude();
        Double secondY = coordsList.get(1).getLatitude();

        double deltaX = firstX - secondX;
        double deltaY = firstY - secondY;

        return Math.sqrt(sq(deltaX) + sq(deltaY));
    }

    public LocationDTO getMidpoint(LocationDTO firstPoint,
                                   LocationDTO secondPoint) {
        Double firstX = firstPoint.getLongitude();
        Double firstY = firstPoint.getLatitude();
        Double secondX = secondPoint.getLongitude();
        Double secondY = secondPoint.getLatitude();

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

    public double sq(double x) {
        return x*x;
    }
}
