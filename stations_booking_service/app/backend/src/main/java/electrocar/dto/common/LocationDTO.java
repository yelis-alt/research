package electrocar.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    @NotBlank
    private Double longitude;

    @NotBlank
    private Double latitude;
}
