package electrocar.dto.evolution;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class GenerationDTO {
    private Map<Integer, Integer> dnaMap;

    private List<Map<Integer, Integer>> parentsList;
}
