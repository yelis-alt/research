package electrocar.service;

import electrocar.dto.FilterStationDTO;
import electrocar.dto.entity.Station;
import electrocar.dto.enums.Plug;
import electrocar.dto.enums.PlugType;
import electrocar.mapper.SimpleBeanRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoutingServiceImpl implements RoutingService {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleBeanRowMapper<Station> rowMapper =
            new SimpleBeanRowMapper<>(Station.class);

    @Override
    public List<Station> getFilteredStations(FilterStationDTO filterStationDTO) {

        String sql = """
                     SELECT * FROM electrocar.station
                     WHERE (plug=:plug OR plug IS NULL) AND
                           (plug_type IN (:plugType) OR plug_type IS NULL) AND
                           ((power>=:fromPower AND power<=:toPower) OR power IS NULL) AND
                           ((price>=:fromPrice AND price<=:toPrice) OR price IS NULL)
                     """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("plug", filterStationDTO.getPlug().toString());
        params.addValue("plugType", filterStationDTO.getPlugType().stream()
                                                              .map(Enum::toString).toList());
        params.addValue("fromPower", filterStationDTO.getFromPower());
        params.addValue("toPower", filterStationDTO.getToPower());
        params.addValue("fromPrice", filterStationDTO.getFromPrice());
        params.addValue("toPrice", filterStationDTO.getToPrice());


        return jdbcTemplate.query(sql, params, rowMapper);
    }
}
