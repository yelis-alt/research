package electrocar.dto.route;

import electrocar.dto.entity.Station;
import electrocar.dto.common.LocationDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
