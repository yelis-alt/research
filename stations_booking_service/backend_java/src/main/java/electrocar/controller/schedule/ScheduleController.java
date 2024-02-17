package electrocar.controller.schedule;

import electrocar.dto.schedule.WindowsRequestDTO;
import electrocar.service.schedule.ScheduleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;

    @GetMapping(value = "/getScheduleWindows")
    public List<String> getScheduleWindows(
            @Valid @RequestBody
            WindowsRequestDTO windowsRequest) {

        return scheduleService.getScheduleWindows(windowsRequest);
    }

    @PostMapping(value = "/postScheduleWindows")
    public void postScheduleWindows(
            @Valid @RequestBody
            List<String> windowsList) {

        scheduleService.postScheduleWindows(windowsList);
    }
}
