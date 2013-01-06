package org.simplefix.dictionary;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a FIX tag definition.
 * <br>
 * User: josh
 * Date: 1/6/13
 * Time: 10:03 AM
 */
public class FieldDef {
    private final int tag;
    private final String name;
    private final String type;
    private final ValueType valueType;
    private final Map<String, String> values;

    public FieldDef(int tag, String name, String type, ValueType valueType, Map<String, String> values) {
        this.tag = tag;
        this.name = name;
        this.type = type;
        this.valueType = valueType;
        this.values = values != null ? Collections.unmodifiableMap(values) : null;
    }

    public int getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldDef)) return false;

        FieldDef fieldDef = (FieldDef) o;

        if (tag != fieldDef.tag) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return tag;
    }

    @Override
    public String toString() {
        return "FieldDef{" +
                "tag=" + tag +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", valueType=" + valueType +
                ", values=" + values +
                '}';
    }
}
