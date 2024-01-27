package electrocar.controller;

import electrocar.dto.LocationDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import electrocar.service.WeatherService;

import java.io.IOException;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/weather")
public class WeatherController {
    private final WeatherService weatherService;

    @GetMapping(value = "/getTemperature")
    public Integer getTemperature(
            @Valid @RequestBody
            LocationDTO locationDTO,
            String date) throws IOException {

        return weatherService.getTemperature(locationDTO, date);
    }
}
