package electrocar.dto.schedule;

import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TimeWindowsSaveRequestDTO {
    private Integer stationId;

    private String code;

    private List<String> timeWindowsList;
}
