package electrocar.dao;

import electrocar.dto.entity.Schedule;
import electrocar.mapper.SimpleBeanRowMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScheduleDaoImpl implements ScheduleDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleBeanRowMapper<Schedule> rowMapper = new SimpleBeanRowMapper<>(Schedule.class);

    @Override
    public List<Schedule> getTimeWindowsByDateAndStationIdsList(String date, List<Integer> stationIdsList) {
        String sql =
                """
					SELECT * FROM electrocar.schedule
					WHERE DATE(datetime_from)::TEXT = :date AND
						id_station IN (:stationIdsList)
					""";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("date", date);
        params.addValue("stationIdsList", stationIdsList);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public void saveTimeWindows(String code, Integer stationId, String datetimeFromString, String datetimeToString) {

        String sql =
                """
					INSERT INTO electrocar.schedule
					(code, id_station, datetime_from, datetime_to)
					VALUES (:code, :stationId,
							CAST(:datetimeFrom AS TIMESTAMP),
							CAST(:datetimeTo AS TIMESTAMP))
					""";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("code", code);
        params.addValue("stationId", stationId);
        params.addValue("datetimeFrom", datetimeFromString);
        params.addValue("datetimeTo", datetimeToString);

        jdbcTemplate.update(sql, params);
    }
}
