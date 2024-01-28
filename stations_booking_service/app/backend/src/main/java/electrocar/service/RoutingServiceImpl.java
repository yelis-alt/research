package electrocar.service;

import electrocar.dto.FilterStationDTO;
import electrocar.dto.entity.Station;
import electrocar.mapper.SimpleBeanRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                     WHERE (plug=:plug OR :plug IS NULL) AND
                           (plug_type IN :plugType OR plug_type IS NULL) AND
                           ((power>=:fromPower AND power<=:toPower) OR power IS NULL) AND
                           ((price>=:fromPrice AND price<=:toPrice) OR price IS NULL)
                     """;
        Map<String, String> mapping = Map.of("plug", filterStationDTO.getPlug(),
                                             "plugType", filterStationDTO.getPlugType(),
                                             "formPower", filterStationDTO.getFromPower().toString(),
                                             "toPower", filterStationDTO.getToPower().toString(),
                                             "formPrice", filterStationDTO.getFromPrice().toString(),
                                             "toPrice", filterStationDTO.getToPrice().toString());

        return jdbcTemplate.query(sql, mapping, rowMapper);
    }
}
