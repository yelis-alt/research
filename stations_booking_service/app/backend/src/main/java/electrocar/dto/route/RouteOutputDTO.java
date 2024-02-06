package electrocar.dto.route;

import electrocar.dto.entity.Station;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class RouteOutputDTO {
    private Station routeNodes;

    private Double reachDuration;
}
