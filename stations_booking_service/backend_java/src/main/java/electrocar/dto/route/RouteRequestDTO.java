package electrocar.dto.route;

import electrocar.dto.common.LocationDTO;
import electrocar.dto.entity.Station;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RouteRequestDTO {

    private LocationDTO startCoords;

    private LocationDTO finishCoords;

    private Integer accLevel;

    private Double accMax;

    private Double spendOpt;

    private Double temperature;

    private List<Station> filteredStationsList;
}
