package electrocar.dao;

import electrocar.dto.entity.Station;
import electrocar.dto.station.FilterStationDTO;

import java.util.List;

public interface StationDao {
    List<Station> getFilteredStations(FilterStationDTO filterStation);
}
