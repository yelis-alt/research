package electrocar.dto.route;

import electrocar.dto.common.DurationDTO;
import electrocar.dto.entity.Station;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class RouteNodeDTO {
    private Station routeNode;

    private Double distance;

    private Double cost;

    private DurationDTO reachDuration;
}
