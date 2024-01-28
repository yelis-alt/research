package electrocar.mapper;

import jakarta.annotation.Nonnull;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.util.HashMap;
import java.util.Map;

public class SimpleBeanRowMapper<T> extends BeanPropertyRowMapper<T> {
    private Map<String, String> remappedFields;

    public SimpleBeanRowMapper(Class<T> clazz) {
        super(clazz);
        setPrimitivesDefaultedForNullValue(true);
        this.remappedFields = new HashMap<>();
    }

    public void remapField(String dbName, String beanName) {
        Map<String, String> newFields = new HashMap<>(remappedFields);
        newFields.put(beanName, dbName.toLowerCase());
        remappedFields = newFields;
        assert this.getMappedClass() != null;
        initialize(this.getMappedClass());
    }

    @Nonnull
    @Override
    protected String underscoreName(@Nonnull String name) {
        if (remappedFields != null) {
            String remapped = remappedFields.get(name);
            if (remapped != null) {
                return remapped;
            }
        }

        StringBuilder r = new StringBuilder();

        boolean prevLower = false;
        for (int i = 0; i < name.length(); i++) {

            char c = name.charAt(i);
            if (Character.isUpperCase(c) && prevLower) {
                r.append('_');
                prevLower = false;
            } else {
                prevLower = true;
            }
            r.append(Character.toLowerCase(c));
        }

        return r.toString();
    }
}
