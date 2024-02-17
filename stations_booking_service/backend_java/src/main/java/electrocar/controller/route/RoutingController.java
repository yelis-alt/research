package electrocar.controller.route;

import electrocar.dto.entity.Station;
import electrocar.dto.route.RouteNodeDTO;
import electrocar.dto.route.RouteRequestDTO;
import electrocar.dto.station.FilterStationDTO;
import electrocar.service.route.RoutingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/routing")
public class RoutingController {
    private final RoutingService routingService;

    @GetMapping(value = "/getFilteredStations")
    public List<Station> getFilteredStation(@Valid @RequestBody FilterStationDTO filterStationDTO) {

        return routingService.getFilteredStations(filterStationDTO);
    }

    @GetMapping(value = "/getRoute")
    public List<RouteNodeDTO> getRoute(@Valid @RequestBody RouteRequestDTO routeRequestDTO) {

        return routingService.getRoute(routeRequestDTO);
    }
}
