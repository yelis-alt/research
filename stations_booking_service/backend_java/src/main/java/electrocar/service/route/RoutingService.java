package electrocar.service.route;

import electrocar.dto.entity.Station;
import electrocar.dto.route.RouteNodeDTO;
import electrocar.dto.route.RouteRequestDTO;
import electrocar.dto.station.FilterStationDTO;
import java.util.List;

public interface RoutingService {
    List<Station> getFilteredStationsList(FilterStationDTO filterStationDTO);

    List<RouteNodeDTO> getRoute(RouteRequestDTO routeRequestDTO);
}
