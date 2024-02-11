package electrocar.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class DurationDTO {
    private Integer hours;

    private Integer minutes;
}
