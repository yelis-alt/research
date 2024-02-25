package electrocar.controller.schedule;

import electrocar.dto.schedule.TimeWindowsRequestDTO;
import electrocar.dto.schedule.TimeWindowsSaveRequestDTO;
import electrocar.service.schedule.ScheduleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule")
public class ScheduleController {
    private static final String STATUS = "status";

    private final ScheduleService scheduleService;

    @PostMapping(value = "/getTimeWindows")
    public Map<String, List<String>> getTimeWindowsMap(@Valid @RequestBody TimeWindowsRequestDTO timeWindowsRequest) {

        return scheduleService.getTimeWindowsMap(timeWindowsRequest);
    }

    @PostMapping(value = "/saveTimeWindows")
    public Map<String, HttpStatus> saveTimeWindows(
            @Valid @RequestBody List<TimeWindowsSaveRequestDTO> timeWindowsSaveRequestsList) {

        scheduleService.saveTimeWindows(timeWindowsSaveRequestsList);

        return Map.of(STATUS, HttpStatus.OK);
    }
}
