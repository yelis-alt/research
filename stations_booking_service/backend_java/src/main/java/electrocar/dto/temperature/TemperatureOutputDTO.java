package electrocar.dto.temperature;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class TemperatureOutputDTO {

    private Double temperature;
}
