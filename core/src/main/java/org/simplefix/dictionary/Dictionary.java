package org.simplefix.dictionary;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * A FIX message / field dictionary.
 * <br>
 * User: josh
 * Date: 9/12/12
 * Time: 4:02 PM
 */
public class Dictionary {

    public Dictionary create(org.simplefix.dictionary.jaxb.Dictionary rootElement) {
        // The DD XML references itself by name, but we don't necessarily need to keep all that around.
        // So, make some maps so we can resolve references.

        Map<String,FieldDef> fieldsByName = Maps.newHashMap();

/*
        List<Field> fields = rootElement.getFields().getField();
        for (Field field : fields) {
            String name = field.getName();
            if (fieldsByName.containsKey(name))
                throw new RuntimeException("Duplicate field name: " + name);
            else
                fieldsByName.put(name,new FieldDef(field.getName(),field.getNumber().intValue(),field.getRequired()));
        }

*/
        return new Dictionary();
    }

    public class FieldDef {


    }
}
