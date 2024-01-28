package electrocar.service;

import electrocar.dto.FilterStationDTO;
import electrocar.dto.entity.Station;

import java.util.List;

public interface RoutingService {
    List<Station> getFilteredStations(FilterStationDTO filterStationDTO);
}
