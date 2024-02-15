package electrocar.dto.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class OpenRouteServiceRequestDTO {

    private List<List<Double>> coordinates;

    @JsonProperty("suppress_warnings")
    private Boolean suppressWarnings;

    private String units;

    private Boolean instructions;

    private String preference;

    private Boolean geometry;
}
