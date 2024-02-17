package electrocar.service.schedule;

import electrocar.dto.schedule.TimeWindowsRequestDTO;
import electrocar.dto.schedule.TimeWindowsSaveRequestDTO;
import java.util.List;

public interface ScheduleService {
    List<String> getTimeWindows(TimeWindowsRequestDTO timeWindowsRequest);

    void saveTimeWindows(TimeWindowsSaveRequestDTO timeWindowsSaveRequest);
}
