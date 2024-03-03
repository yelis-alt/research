package electrocar.dto.entity;

import java.time.Instant;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "schedule", schema = "electrocar")
public class Schedule {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "id_station", nullable = false)
    private Integer idStation;

    @Column(name = "datetime_from", nullable = false)
    private Instant datetimeFrom;

    @Column(name = "datetime_to", nullable = false)
    private Instant datetimeTo;
}
