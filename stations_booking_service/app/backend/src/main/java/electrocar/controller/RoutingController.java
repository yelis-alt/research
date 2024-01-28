package electrocar.controller;

import electrocar.dto.FilterStationDTO;
import electrocar.dto.LocationDTO;
import electrocar.dto.entity.Station;
import electrocar.service.RoutingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/routing")
public class RoutingController {
    private RoutingService routingService;

    @GetMapping(value = "/getFilteredStations")
    public List<Station> getFilteredStation(
            @Valid @RequestBody
            FilterStationDTO filterStationDTO) {

        return routingService.getFilteredStations(filterStationDTO);
    }

}
