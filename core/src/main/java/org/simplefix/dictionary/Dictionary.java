package org.simplefix.dictionary;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;

/**
 * A FIX message / field dictionary.
 * <br>
 * User: josh
 * Date: 9/12/12
 * Time: 4:02 PM
 */
public class Dictionary {
    private final Map<String,FieldDef> fieldsByName;

    public Dictionary(Map<String, FieldDef> fieldsByName) {
        this.fieldsByName = Collections.unmodifiableMap(fieldsByName);
    }

    public enum FieldType {
        INT,
    }

    public static class FieldDef {
        private final int tag;
        private final String name;
        private final FieldType type;
        private final Map<String,String> values;

        public FieldDef(int tag, String name, FieldType type, Map<String, String> values) {
            this.tag = tag;
            this.name = name;
            this.type = type;
            this.values = values != null ? Collections.unmodifiableMap(values) : null;
        }

        public int getTag() {
            return tag;
        }

        public String getName() {
            return name;
        }

        public FieldType getType() {
            return type;
        }
    }

}
