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
    @NotBlank
    private LocationDTO startCoords;

    @NotBlank
    private LocationDTO finishCoords;

    @NotBlank
    private List<Station> filteredStationsList;
}
