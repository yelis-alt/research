package electrocar.dto.schedule;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TimeWindowsRequestDTO {
    private String date;

    private Integer stationId;
}
