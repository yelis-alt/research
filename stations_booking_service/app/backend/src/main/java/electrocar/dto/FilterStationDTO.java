package electrocar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FilterStationDTO {
    @NotBlank
    private String plug;
    @NotBlank
    private String plugType;
    @NotBlank
    private Integer fromPower;
    @NotBlank
    private Integer toPower;
    @NotBlank
    private Integer fromPrice;
    @NotBlank
    private Integer toPrice;
}
