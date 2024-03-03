package electrocar.controller.schedule;

import electrocar.dto.schedule.TimeWindowsOutputDTO;
import electrocar.dto.schedule.TimeWindowsRequestDTO;
import electrocar.dto.schedule.TimeWindowsSaveOutputDTO;
import electrocar.dto.schedule.TimeWindowsSaveRequestDTO;
import electrocar.service.schedule.ScheduleService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PostMapping(value = "/getTimeWindows")
    public List<TimeWindowsOutputDTO> getTimeWindowsMap(@Valid @RequestBody TimeWindowsRequestDTO timeWindowsRequest) {

        return scheduleService.getTimeWindows(timeWindowsRequest);
    }

    @PostMapping(value = "/saveTimeWindows")
    public TimeWindowsSaveOutputDTO saveTimeWindows(
            @Valid @RequestBody List<TimeWindowsSaveRequestDTO> timeWindowsSaveRequestsList) {

        scheduleService.saveTimeWindows(timeWindowsSaveRequestsList);

        return new TimeWindowsSaveOutputDTO(HttpStatus.OK);
    }
}
