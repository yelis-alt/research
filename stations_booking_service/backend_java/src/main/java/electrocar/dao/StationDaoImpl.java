package electrocar.dao;

import electrocar.dto.entity.Station;
import electrocar.dto.station.FilterStationDTO;
import electrocar.mapper.SimpleBeanRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StationDaoImpl implements StationDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleBeanRowMapper<Station> rowMapper =
            new SimpleBeanRowMapper<>(Station.class);

    @Override
    public List<Station> getFilteredStations(FilterStationDTO filterStation) {
        String sql = """
                     SELECT * FROM electrocar.station
                     WHERE (plug=:plug OR plug IS NULL) AND
                           (plug_type IN (:plugType) OR plug_type IS NULL) AND
                           ((power>=:fromPower AND power<=:toPower) OR power IS NULL) AND
                           ((price>=:fromPrice AND price<=:toPrice) OR price IS NULL)
                     """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("plug", filterStation.getPlug().toString());
        params.addValue("plugType", filterStation.getPlugType().stream()
                .map(Enum::toString).toList());
        params.addValue("fromPower", filterStation.getFromPower());
        params.addValue("toPower", filterStation.getToPower());
        params.addValue("fromPrice", filterStation.getFromPrice());
        params.addValue("toPrice", filterStation.getToPrice());

        return jdbcTemplate.query(sql, params, rowMapper);
    }
}
