package electrocar.dto.schedule;

import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TimeWindowsOutputDTO {
    private Integer stationId;

    private List<String> timeWindowsList;
}
