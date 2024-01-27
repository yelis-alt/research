package electrocar.service;

import electrocar.dto.LocationDTO;

import java.io.IOException;

public interface WeatherService {
    Integer getTemperature(LocationDTO locationDTO, String date) throws IOException;
}
