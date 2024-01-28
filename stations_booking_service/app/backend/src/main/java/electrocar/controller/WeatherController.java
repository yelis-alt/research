package electrocar.controller;

import electrocar.dto.LocationDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
            @NotBlank
            String date) throws IOException, ParseException {

        return weatherService.getTemperature(locationDTO, date);
    }
}
