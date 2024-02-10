package electrocar.service.weather;

import electrocar.dto.common.LocationDTO;
import electrocar.dto.temperature.TemperatureOutputDTO;

import java.io.IOException;
import java.text.ParseException;

public interface WeatherService {
    TemperatureOutputDTO getTemperature(LocationDTO locationDTO, String date)
            throws IOException, ParseException;
}
