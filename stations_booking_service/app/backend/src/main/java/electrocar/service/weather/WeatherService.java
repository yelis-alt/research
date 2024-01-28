package electrocar.service.weather;

import electrocar.dto.common.LocationDTO;

import java.io.IOException;
import java.text.ParseException;

public interface WeatherService {
    Integer getTemperature(LocationDTO locationDTO, String date) throws IOException, ParseException;
}
