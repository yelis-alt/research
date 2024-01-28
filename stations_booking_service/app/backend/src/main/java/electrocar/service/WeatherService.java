package electrocar.service;

import electrocar.dto.LocationDTO;

import java.io.IOException;
import java.text.ParseException;

public interface WeatherService {
    Integer getTemperature(LocationDTO locationDTO, String date) throws IOException, ParseException;
}
