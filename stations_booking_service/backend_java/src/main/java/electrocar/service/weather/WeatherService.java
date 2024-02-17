package electrocar.service.weather;

import electrocar.dto.common.LocationDTO;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface WeatherService {
    List<Double> getTemperature(LocationDTO locationDTO, String date)
            throws IOException, ParseException;
}
