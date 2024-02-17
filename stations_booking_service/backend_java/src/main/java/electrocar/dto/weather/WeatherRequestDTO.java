package electrocar.dto.weather;

import electrocar.dto.common.LocationDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class WeatherRequestDTO {
    private LocationDTO location;

    private String date;
}
