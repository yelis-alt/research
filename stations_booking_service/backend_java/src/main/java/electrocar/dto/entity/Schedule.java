package electrocar.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "station", schema = "electrocar")
public class Schedule {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "code", nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "id_station", referencedColumnName = "id")
    private Station station;

    @Column(name = "datetime_from", nullable = false)
    private Instant datetimeFrom;

    @Column(name = "datetime_to", nullable = false)
    private Instant datetimeTo;
}
