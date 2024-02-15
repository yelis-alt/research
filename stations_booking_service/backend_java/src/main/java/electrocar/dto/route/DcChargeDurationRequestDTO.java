package electrocar.dto.route;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class DcChargeDurationRequestDTO {
    private List<Double> energy;

    private List<Double> temperature;
}
