package electrocar.dao;

import electrocar.dto.entity.Schedule;
import electrocar.dto.entity.Station;
import electrocar.mapper.SimpleBeanRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ScheduleDaoImpl implements ScheduleDao{
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleBeanRowMapper<Schedule> rowMapper =
            new SimpleBeanRowMapper<>(Schedule.class);

    @Override
    public List<Schedule> getWindowsByDateAndStationId(String date, Integer stationId) {
        String sql = """
                     SELECT * FROM electrocar.schedule
                     WHERE DATE(datetime_from) = :date AND
                           id_station = :stationId
                     """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("date", date);
        params.addValue("stationId", stationId);

        return jdbcTemplate.query(sql, params, rowMapper);
    }
}
