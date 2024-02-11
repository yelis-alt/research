package electrocar.service.route;

import electrocar.dto.route.RouteRequestDTO;
import electrocar.dto.station.FilterStationDTO;
import electrocar.dto.route.RouteNodeDTO;
import electrocar.dto.entity.Station;

import java.util.List;

public interface RoutingService {
    List<Station> getFilteredStations(FilterStationDTO filterStationDTO);

    List<RouteNodeDTO> getRoute(RouteRequestDTO routeRequestDTO);
}
