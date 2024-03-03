package electrocar.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlugType {
    AC("ac"),
    DC("dc");

    private final String name;
}
