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
    private final Map<Integer, FieldDef> fieldsByTag;
    private final Map<String, MessageType> messageTypes;

    /**
     * Creates the dictionary.
     * @param fieldsByName Map of fields by their name.  Tags should be unique as well, but that
     *                     should be checked by the caller.
     * @param messageTypes Map of message types by their names.
     */
    public Dictionary(Map<String, FieldDef> fieldsByName, Map<String, MessageType> messageTypes) {
        this.fieldsByName = Collections.unmodifiableMap(fieldsByName);
        Map<Integer,FieldDef> byTag = Maps.newHashMapWithExpectedSize(fieldsByName.size());
        for (FieldDef fieldDef : fieldsByName.values()) {
            byTag.put(fieldDef.getTag(),fieldDef);
        }
        this.fieldsByTag = Collections.unmodifiableMap(byTag);
        this.messageTypes = Collections.unmodifiableMap(messageTypes);
    }

    /**
     * Returns the field definition by it's tag.
     * @param tag the tag
     * @return field definition
     */
    public FieldDef getFieldDef(int tag) {
        return fieldsByTag.get(tag);
    }

    /**
     * Returns the field definition by it's name.
     * @param name the name
     * @return field definition
     */
    public FieldDef getFieldDef(String name) {
        return fieldsByName.get(name);
    }

    public MessageType getMessageType(String msgType) {
        return messageTypes.get(msgType);
    }
}
