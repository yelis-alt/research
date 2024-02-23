package electrocar.dao;

import electrocar.dto.entity.Schedule;
import java.util.List;

public interface ScheduleDao {
    List<Schedule> getTimeWindowsByDateAndStationIdsList(String date, List<Integer> stationIdsList);

    void saveTimeWindows(String code, Integer stationId, String datetimeFromString, String datetimeToString);
}
