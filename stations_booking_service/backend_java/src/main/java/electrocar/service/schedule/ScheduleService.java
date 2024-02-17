package electrocar.service.schedule;

import electrocar.dto.schedule.WindowsRequestDTO;

import java.util.List;

public interface ScheduleService {
    List<String> getScheduleWindows(WindowsRequestDTO windowsRequest);

    void postScheduleWindows(List<String> windowsList);
}
