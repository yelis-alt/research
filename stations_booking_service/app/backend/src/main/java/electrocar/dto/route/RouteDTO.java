package electrocar.dto.route;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RouteDTO {
    private Integer stationId;

    private String reachTime;
}
