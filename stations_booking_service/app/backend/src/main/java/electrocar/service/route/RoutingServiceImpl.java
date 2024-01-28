package electrocar.service.route;

import electrocar.dto.common.LocationDTO;
import electrocar.dto.enums.PlugType;
import electrocar.dto.station.FilterStationDTO;
import electrocar.dto.route.RouteOutputDTO;
import electrocar.dto.route.RouteRequestDTO;
import electrocar.dto.entity.Station;
import electrocar.mapper.SimpleBeanRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoutingServiceImpl implements RoutingService {
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


        return null;
    }


    public Map<Integer, Map<Integer, Double>> getAdjacencyMatrix(RouteRequestDTO routeRequestDTO,
                                                                 Station startPoint,
                                                                 Station finishPoint) {
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
        finishPoint.setId(nodesList.size());
        nodesList.add(finishPoint);
        nodesList = nodesList.stream()
                             .sorted(Comparator.comparing(Station::getId))
                             .toList();





        return new HashMap<>();
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
            double dist = getEuclideanDist(List.of(midpoint, stationCoords));

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
