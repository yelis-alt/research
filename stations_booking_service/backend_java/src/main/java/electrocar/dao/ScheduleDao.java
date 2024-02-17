package electrocar.dao;

import electrocar.dto.entity.Schedule;

import java.util.List;

public interface ScheduleDao {
    List<Schedule> getWindowsByDateAndStationId(String date, Integer stationId);
}
