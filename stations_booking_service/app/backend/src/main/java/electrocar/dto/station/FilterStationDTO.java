package electrocar.dto.station;

import electrocar.dto.enums.Plug;
import electrocar.dto.enums.PlugType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class FilterStationDTO {
    private Plug plug;

    private List<PlugType> plugType;

    @NotNull
    private Integer fromPower;

    @NotNull
    private Integer toPower;

    @NotNull
    private Integer fromPrice;

    @NotNull
    private Integer toPrice;
}
