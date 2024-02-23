package electrocar.controller.schedule;

import electrocar.dto.schedule.TimeWindowsRequestDTO;
import electrocar.dto.schedule.TimeWindowsSaveRequestDTO;
import electrocar.service.schedule.ScheduleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PostMapping(value = "/getTimeWindows")
    public Map<String, List<String>> getTimeWindowsMap(@Valid @RequestBody TimeWindowsRequestDTO timeWindowsRequest) {

        return scheduleService.getTimeWindowsMap(timeWindowsRequest);
    }

    @PostMapping(value = "/saveTimeWindows")
    public void saveTimeWindows(@Valid @RequestBody TimeWindowsSaveRequestDTO timeWindowsSaveRequest) {

        scheduleService.saveTimeWindows(timeWindowsSaveRequest);
    }
}
