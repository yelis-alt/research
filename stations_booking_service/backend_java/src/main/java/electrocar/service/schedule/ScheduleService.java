package electrocar.service.schedule;

import electrocar.dto.schedule.TimeWindowsRequestDTO;
import electrocar.dto.schedule.TimeWindowsSaveRequestDTO;
import java.util.List;
import java.util.Map;

public interface ScheduleService {
    Map<String, List<String>> getTimeWindowsMap(TimeWindowsRequestDTO timeWindowsRequest);

    void saveTimeWindows(List<TimeWindowsSaveRequestDTO> timeWindowsSaveRequestsList);
}
