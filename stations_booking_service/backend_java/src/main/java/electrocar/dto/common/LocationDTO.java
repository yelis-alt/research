package electrocar.dto.common;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    @NotNull
    private Double longitude;

    @NotNull
    private Double latitude;
}
