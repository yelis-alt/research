package electrocar.service.schedule;

import electrocar.dao.ScheduleDao;
import electrocar.dto.entity.Schedule;
import electrocar.dto.schedule.TimeWindowsRequestDTO;
import electrocar.dto.schedule.TimeWindowsSaveRequestDTO;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
    private static final String SECONDS_PART = ":00";

    private final ScheduleDao scheduleDao;

    @Override
    public List<String> getTimeWindows(TimeWindowsRequestDTO timeWindowsRequest) {
        List<Schedule> schedulesList = scheduleDao.getTimeWindowsByDateAndStationId(
                timeWindowsRequest.getDate(), timeWindowsRequest.getStationId());

        return schedulesList.stream().map(this::getTimeWindowString).toList();
    }

    @Override
    public void saveTimeWindows(TimeWindowsSaveRequestDTO timeWindowsSaveRequest) {
        for (String timeWindow : timeWindowsSaveRequest.getTimeWindowsList()) {
            String date = timeWindow.split(" ")[0].replace(".", "-");
            String timePeriod = timeWindow.split(" ")[1];
            String startTime = timePeriod.split("-")[0];
            String finishTime = timePeriod.split("-")[1];

            String datetimeFromString = date + " " + startTime + SECONDS_PART;
            String datetimeToString = date + " " + finishTime + SECONDS_PART;

            scheduleDao.saveTimeWindows(
                    timeWindowsSaveRequest.getCode(),
                    timeWindowsSaveRequest.getStationId(),
                    datetimeFromString,
                    datetimeToString);
        }
    }

    public String getTimeWindowString(Schedule schedule) {
        Date date = Date.from(schedule.getDatetimeFrom());
        String dateString = formatter.format(date);

        int hourFrom = schedule.getDatetimeFrom().atZone(ZoneOffset.UTC).getHour();
        int minuteFrom = schedule.getDatetimeFrom().atZone(ZoneOffset.UTC).getMinute();
        int hourTo = schedule.getDatetimeTo().atZone(ZoneOffset.UTC).getHour();
        int minuteTo = schedule.getDatetimeTo().atZone(ZoneOffset.UTC).getMinute();

        return dateString
                + " "
                + addTrailingZero(hourFrom)
                + ":"
                + addTrailingZero(minuteFrom)
                + "-"
                + addTrailingZero(hourTo)
                + ":"
                + addTrailingZero(minuteTo);
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
