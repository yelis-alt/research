package electrocar.service.schedule;

import electrocar.dao.ScheduleDao;
import electrocar.dto.entity.Schedule;
import electrocar.dto.schedule.WindowsRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService{
    private static final SimpleDateFormat formatter =
            new SimpleDateFormat("dd.MM.yyyy");

    private final ScheduleDao scheduleDao;

    @Override
    public List<String> getScheduleWindows(WindowsRequestDTO windowsRequest) {
        List<Schedule> schedulesList =
                scheduleDao.getWindowsByDateAndStationId(
                        windowsRequest.getDate(), windowsRequest.getStationId());

        return schedulesList.stream()
                .map(this::getWindowString).toList();
    }

    @Override
    public void postScheduleWindows(List<String> windowsList) {

    }
    
    public String getWindowString(Schedule schedule) {
        Date date = Date.from(schedule.getDatetimeFrom());
        String dateString = formatter.format(date);

        int hourFrom = schedule.getDatetimeFrom().atZone(ZoneOffset.UTC).getHour();
        int minuteFrom = schedule.getDatetimeFrom().atZone(ZoneOffset.UTC).getMinute();
        int hourTo = schedule.getDatetimeTo().atZone(ZoneOffset.UTC).getHour();
        int minuteTo = schedule.getDatetimeTo().atZone(ZoneOffset.UTC).getMinute();

        return dateString +
                " " +
                addTrailingZero(hourFrom) +
                ":" +
                addTrailingZero(minuteFrom) +
                "-" +
                addTrailingZero(hourTo) +
                ":" +
                addTrailingZero(minuteTo);
    }

    public String addTrailingZero(int time) {
        String timeString = String.valueOf(time);

        if (timeString.length() == 1) {
            return "0" + timeString;
        } else {
            return timeString;
        }
    }
}



