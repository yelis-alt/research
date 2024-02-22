package electrocar.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Plug {
    TYPE_2("type2"),
    CHADEMO("chademo"),
    CCSCOMBO_1("ccscombo1"),
    CCSCOMBO_2("ccscombo2"),
    SAEJ_1772("saej1772"),
    TESLA("tesla"),
    GBTAC("gbtac"),
    GBTDC("gbtdc");

    private final String name;
}
