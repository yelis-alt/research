package electrocar.controller.weather;

import electrocar.dto.common.LocationDTO;
import electrocar.service.weather.WeatherService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/weather")
public class WeatherController {
    private final WeatherService weatherService;

    @GetMapping(value = "/getTemperature")
    public List<Double> getTemperature(@Valid @RequestBody LocationDTO location, @NotBlank @RequestParam String date)
            throws IOException, ParseException {

        return weatherService.getTemperature(location, date);
    }
}
