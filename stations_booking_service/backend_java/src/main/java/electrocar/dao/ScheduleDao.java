package electrocar.dao;

import electrocar.dto.entity.Schedule;
import java.util.List;

public interface ScheduleDao {
    List<Schedule> getTimeWindowsByDateAndStationId(String date, Integer stationId);

    void saveTimeWindows(String code, Integer stationId, String datetimeFromString, String datetimeToString);
}
