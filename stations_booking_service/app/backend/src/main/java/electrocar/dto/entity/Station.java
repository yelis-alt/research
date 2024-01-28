package electrocar.dto.entity;

import electrocar.dto.enums.Plug;
import electrocar.dto.enums.PlugType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "station", schema = "electrocar")
public class Station {
    @Id
    @NotBlank
    private Integer id;

    private String address;

    @NotBlank
    private BigDecimal latitude;

    @NotBlank
    private BigDecimal longitude;

    private Integer price;

    private String company;

    @Enumerated(EnumType.STRING)
    private Plug plug;

    private Integer power;

    @Enumerated(EnumType.STRING)
    private PlugType plugType;

    @NotBlank
    private Boolean status;
}
