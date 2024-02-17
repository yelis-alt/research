package electrocar.dto.schedule;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class WindowsRequestDTO {
    private String date;

    private Integer stationId;
}
