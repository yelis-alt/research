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
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "company", nullable = false)
    private String company;

    @Enumerated(EnumType.STRING)
    @Column(name = "plug", nullable = false)
    private Plug plug;

    @Column(name = "power", nullable = false)
    private Integer power;

    @Enumerated(EnumType.STRING)
    @Column(name = "plug_type", nullable = false)
    private PlugType plugType;

    @NotBlank
    @Column(name = "status", nullable = false)
    private Boolean status;
}
