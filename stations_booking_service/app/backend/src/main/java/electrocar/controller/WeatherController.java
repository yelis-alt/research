package electrocar.controller;

import electrocar.dto.LocationDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import electrocar.service.WeatherService;

import java.io.IOException;
import java.text.ParseException;

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
            @NotBlank @RequestParam
            String date) throws IOException, ParseException {

        return weatherService.getTemperature(locationDTO, date);
    }
}
