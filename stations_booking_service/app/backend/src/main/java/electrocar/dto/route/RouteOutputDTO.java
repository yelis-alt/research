package electrocar.dto.route;

import electrocar.dto.entity.Station;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class RouteOutputDTO {
    private List<RouteDTO> route;

    private List<Station> stations;
}
