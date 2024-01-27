package electrocar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LocationDTO {
    @NotBlank
    private String latitude;
    @NotBlank
    private String longitude;
}
