package electrocar.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class TimeWindowsSaveOutputDTO {
    private HttpStatus status;
}
