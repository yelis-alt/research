package electrocar.service.route;

import electrocar.dto.route.RouteRequestDTO;
import electrocar.dto.station.FilterStationDTO;
import electrocar.dto.route.RouteOutputDTO;
import electrocar.dto.entity.Station;

import java.util.List;

public interface RoutingService {
    List<Station> getFilteredStations(FilterStationDTO filterStationDTO);

    List<RouteOutputDTO> getRoute(RouteRequestDTO routeRequestDTO);
}
