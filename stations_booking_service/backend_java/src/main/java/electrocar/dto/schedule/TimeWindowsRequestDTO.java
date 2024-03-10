package electrocar.dto.schedule;

import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TimeWindowsRequestDTO {
    private String date;

    private List<Integer> stationIdsList;
}
