package electrocar.service.weather;

import electrocar.dto.weather.WeatherRequestDTO;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface WeatherService {
    List<Double> getTemperature(WeatherRequestDTO weatherRequest) throws IOException, ParseException;
}
