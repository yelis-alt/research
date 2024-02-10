package electrocar.dto.route;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class DcChargeDurationOutputDTO {
    private Double timeCharge;
}
