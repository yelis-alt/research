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
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/routing")
public class RoutingController {
    private final RoutingService routingService;

    @PostMapping(value = "/getFilteredStations")
    public List<Station> getFilteredStationsList(@Valid @RequestBody FilterStationDTO filterStation) {

        return routingService.getFilteredStationsList(filterStation);
    }

    @PostMapping(value = "/getRoute")
    public List<RouteNodeDTO> getRoute(@Valid @RequestBody RouteRequestDTO routeRequestDTO) {

        return routingService.getRoute(routeRequestDTO);
    }
}
