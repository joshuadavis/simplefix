package org.simplefix.dictionary;

import com.google.common.collect.Maps;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Map;

/**
 * Builds immutable Dictionary objects.
 * <br>
 * User: josh
 * Date: 1/6/13
 * Time: 8:22 AM
 */
public class DictionaryBuilder {
    private final Map<String, FieldDef> fieldsByName;
    private final Map<Integer, FieldDef> fieldsByTag;
    private final Map<String, ValueType> valueTypeMap;
    private final Map<String, MessageType> messageTypes;

    public DictionaryBuilder(Map<String,ValueType> valueTypeMap) {
        fieldsByName = Maps.newHashMap();
        fieldsByTag = Maps.newHashMap();
        this.valueTypeMap = valueTypeMap;
        messageTypes = Maps.newHashMap();
    }

    public boolean containsFieldName(String name) {
        return fieldsByName.containsKey(name);
    }

    public boolean containsTag(int tag) {
        return fieldsByTag.containsKey(tag);
    }

    public void addFieldDef(FieldDef fieldDef) {
        fieldsByName.put(fieldDef.getName(),fieldDef);
        fieldsByTag.put(fieldDef.getTag(),fieldDef);
    }

    public Dictionary create() {
        return new Dictionary(fieldsByName,messageTypes);
    }

    public ValueType mapToValueType(String typeString) {
        ValueType v = valueTypeMap.get(typeString);
        return (v == null) ? ValueType.ANY : v;
    }

    public FieldDef getFieldDef(String fieldName) {
        return fieldsByName.get(fieldName);
    }

    public void addMessageType(MessageType messageType) {
        messageTypes.put(messageType.getMsgType(),messageType);
    }
}
