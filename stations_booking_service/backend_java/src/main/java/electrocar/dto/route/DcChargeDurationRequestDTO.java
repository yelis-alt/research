package electrocar.dto.route;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class DcChargeDurationRequestDTO {
    private List<Double> energy;

    private List<Double> temperature;
}
