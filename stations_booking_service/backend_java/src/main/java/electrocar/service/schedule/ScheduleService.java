package electrocar.service.schedule;

import electrocar.dto.schedule.TimeWindowsOutputDTO;
import electrocar.dto.schedule.TimeWindowsRequestDTO;
import electrocar.dto.schedule.TimeWindowsSaveRequestDTO;
import java.util.List;

public interface ScheduleService {
    List<TimeWindowsOutputDTO> getTimeWindows(TimeWindowsRequestDTO timeWindowsRequest);

    void saveTimeWindows(List<TimeWindowsSaveRequestDTO> timeWindowsSaveRequestsList);
}
