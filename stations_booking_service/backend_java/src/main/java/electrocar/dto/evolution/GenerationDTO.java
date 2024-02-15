package electrocar.dto.evolution;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class GenerationDTO {
    private Map<Integer, Integer> dnaMap;

    private List<Map<Integer, Integer>> parentsList;
}
