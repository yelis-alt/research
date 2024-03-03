package electrocar.service.schedule;

import electrocar.dao.ScheduleDao;
import electrocar.dto.entity.Schedule;
import electrocar.dto.schedule.TimeWindowsOutputDTO;
import electrocar.dto.schedule.TimeWindowsRequestDTO;
import electrocar.dto.schedule.TimeWindowsSaveRequestDTO;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private static final String SECONDS_PART = ":00";

    private final SimpleDateFormat formatter;
    private final ScheduleDao scheduleDao;

    @Override
    public List<TimeWindowsOutputDTO> getTimeWindows(TimeWindowsRequestDTO timeWindowsRequest) {
        List<Integer> stationIdsList = timeWindowsRequest.getStationIdsList();
        List<Schedule> schedulesList =
                scheduleDao.getTimeWindowsByDateAndStationIdsList(timeWindowsRequest.getDate(), stationIdsList);

        Map<Integer, List<String>> timeWindowMap = new HashMap<>();
        if (schedulesList.isEmpty()) {
            for (Integer stationId : stationIdsList) {
                timeWindowMap.put(stationId, new ArrayList<>());
            }
        } else {
            for (Schedule schedule : schedulesList) {
                int stationId = schedule.getIdStation();

                if (!timeWindowMap.containsKey(stationId)) {
                    timeWindowMap.put(stationId, new ArrayList<>());
                }
                timeWindowMap.get(stationId).add(getTimeWindowString(schedule));
            }

            for (Integer stationId : stationIdsList) {
                if (!timeWindowMap.containsKey(stationId)) {
                    timeWindowMap.put(stationId, new ArrayList<>());
                }
            }
        }

        List<TimeWindowsOutputDTO> timeWindowsOutpusList = new ArrayList<>();
        for (Map.Entry<Integer, List<String>> entry : timeWindowMap.entrySet()) {
            TimeWindowsOutputDTO timeWindowsOutput = new TimeWindowsOutputDTO();
            timeWindowsOutput.setStationId(entry.getKey());
            timeWindowsOutput.setTimeWindowsList(entry.getValue());

            timeWindowsOutpusList.add(timeWindowsOutput);
        }

        return timeWindowsOutpusList;
    }

    @Override
    @Transactional
    public void saveTimeWindows(List<TimeWindowsSaveRequestDTO> timeWindowsSaveRequestsList) {
        for (TimeWindowsSaveRequestDTO timeWindowsSaveRequest : timeWindowsSaveRequestsList) {
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
