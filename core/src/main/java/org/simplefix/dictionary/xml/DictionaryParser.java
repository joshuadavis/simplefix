package org.simplefix.dictionary.xml;

import com.google.common.collect.Maps;
import org.simplefix.dictionary.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parses QFJ data dictionary XML files.
 * <br/>
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 9/7/12
 * Time: 8:33 PM
 */
public class DictionaryParser {

    private static final Logger log = LoggerFactory.getLogger(DictionaryParser.class);

    /**
     * Builder for the current field definition.
     */
    private FieldDefBuilder currentField;

    /**
     * The current message type.
     */
    private MessageDefBuilder currentMessage;

    private final Map<String,MessageDefBuilder> messageTypes;

    private final DictionaryBuilder builder;

    private Map<String, ValueType> readValueTypeMap() {
        try {
            Map<String,ValueType> valueTypeMap = Maps.newHashMap();
            XMLEventReader eventReader = StAXHelper.createXMLEventReaderForResource("org/simplefix/value-types.xml");
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        StartElement startElement = event.asStartElement();
                        if ("value-type".equals(startElement.getName().getLocalPart())) {
                            String type = StAXHelper.stringAttribute(startElement,"type");
                            ValueType valueType = ValueType.valueOf(
                                    StAXHelper.stringAttribute(startElement,"valueType"));
                            valueTypeMap.put(type,valueType);
                        }
                        break;
                    default:
                        break;
                }
            }
            return valueTypeMap;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public DictionaryParser() {
        builder = new DictionaryBuilder(readValueTypeMap());
        currentField = null;
        messageTypes = Maps.newHashMap();
    }

    public static Dictionary parseXML(URL url) throws DictionaryParseException {
        XMLEventReader eventReader = StAXHelper.createXMLEventReader(url);
        try {
            return new DictionaryParser().doParse(eventReader);
        } catch (XMLStreamException e) {
            throw new DictionaryParseException(parseError(url, e), e);
        } catch (DictionaryParseException dpe) {
            parseError(url, dpe);
            throw dpe;
        }
    }

    private static String parseError(URL url, Throwable e) {
        final String s = "Unable to parse " + url + " due to " + e;
        log.error(s, e);
        return s;
    }

    private String getErrorLocation(XMLEvent event) {
        return StAXHelper.getLocationString(event);
    }

    private class FieldDefBuilder {
        private final int tag;
        private final String name;
        private final String type;
        private final Map<String, String> values = Maps.newHashMap();
        private final ValueType valueType;

        private FieldDefBuilder(StartElement startElement) {
            tag = StAXHelper.intAttribute(startElement, "number");
            name = StAXHelper.stringAttribute(startElement, "name");
            type = StAXHelper.stringAttribute(startElement, "type");
            valueType = builder.mapToValueType(type);
        }

        public FieldDef createFieldDef() {
            return new FieldDef(tag, name, type, valueType, values);
        }

        public void addValue(StartElement startElement) {
            String key = StAXHelper.stringAttribute(startElement, "enum");
            String name = StAXHelper.stringAttribute(startElement, "description");
            if (values.containsKey(key))
                throw new DictionaryParseException("Duplicate value " + startElement +
                        getErrorLocation(startElement));
            values.put(key, name);
        }
    }

    private Dictionary doParse(XMLEventReader eventReader) throws XMLStreamException {
        PathAwareXMLEventReader pathReader = new PathAwareXMLEventReader(eventReader);
        while (pathReader.hasNext()) {
            PathEvent event = pathReader.nextEvent();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    startElement(event);
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    endElement(event);
                    break;
            }
        }

        resolveReferences();
        // Parse all the message types link the fields up by name.
        return builder.create();
    }

    private void resolveReferences() {
        // Resolve field references in the message types.
        for (MessageDefBuilder messageDefBuilder : messageTypes.values()) {
            final LinkedHashMap<String, StartElement> fieldRefs = messageDefBuilder.fieldRefs;
            LinkedHashMap<Integer,FieldRef> refMap = new LinkedHashMap<Integer, FieldRef>(fieldRefs.size());
            for (StartElement fieldRef : fieldRefs.values()) {
                String fieldName = StAXHelper.stringAttribute(fieldRef, "name");
                FieldDef fieldDef = builder.getFieldDef(fieldName);
                if (fieldDef == null) {
                    throw new DictionaryParseException("Undefined field '" + fieldName + "' " +
                            getErrorLocation(fieldRef));
                }
                boolean required = StAXHelper.booleanAttribute(fieldRef,"required");
                FieldRef ref = new FieldRef(fieldDef,required);
                refMap.put(fieldDef.getTag(),ref);
            }
            String msgType = messageDefBuilder.getMsgType();
            boolean applicationMessage = !"admin".equalsIgnoreCase(
                    StAXHelper.stringAttribute(messageDefBuilder.startElement, "msgcat"));
            MessageType messageType = new MessageType(msgType, applicationMessage, refMap);
            builder.addMessageType(messageType);
        }
    }

    private void endElement(PathEvent event) {
        EndElement endElement = event.asEndElement();
        final String pathString = event.getPath();
        if (pathString.startsWith("fix/fields")) {
            if ("fix/fields/field".equals(pathString)) {
                fieldDef(endElement, event);
            }
        } else if (pathString.startsWith("fix/messages")) {
            if ("fix/messages/message".equals(pathString)) {
                messageDef(event);
            }
        }
    }

    private void messageDef(PathEvent e) {
        if (messageTypes.containsKey(currentMessage.getMsgType())) {
            throw new DictionaryParseException("Duplicate message type '" + currentMessage.getMsgType() + "' " +
                    getErrorLocation(e.getStart()));
        }
        messageTypes.put(currentMessage.getMsgType(), currentMessage);
    }

    private void fieldDef(EndElement endElement, PathEvent e) {
        // We're done with a field definition.
        if (currentField == null)
            throw new DictionaryParseException("No field definition! at " + endElement.getLocation());
        FieldDef fieldDef = currentField.createFieldDef();
        if (builder.containsFieldName(fieldDef.getName())) {
            throw new DictionaryParseException("Duplicate field '" + fieldDef.getName() + "' " +
                    getErrorLocation(e.getStart()));
        }
        if (builder.containsTag(fieldDef.getTag())) {
            throw new DictionaryParseException("Duplicate tag '" + fieldDef.getTag() + "' at " +
                    getErrorLocation(e.getStart()));
        }
        builder.addFieldDef(fieldDef);
        currentField = null;
    }

    private void startElement(PathEvent event) {
        StartElement startElement = event.asStartElement();
        final String pathString = event.getPath();
        if (pathString.startsWith("fix/fields")) {
            if ("fix/fields/field".equals(pathString)) {
                currentField = new FieldDefBuilder(startElement);
            } else if ("fix/fields/field/value".equals(pathString)) {
                valueDef(startElement);
            }
        } else if (pathString.startsWith("fix/messages")) {
            if ("fix/messages/message".equals(pathString)) {
                currentMessage = new MessageDefBuilder(startElement);
            } else if ("fix/messages/message/field".equals(pathString)) {
                fieldRef(currentMessage,startElement);
            }
        }
    }

    private void fieldRef(MessageDefBuilder defBuilder, StartElement fieldRefElement) {
        if (defBuilder == null) {
            throw new DictionaryParseException("Field reference is not inside a group! " +
                    getErrorLocation(fieldRefElement));
        }
        defBuilder.addFieldRef(fieldRefElement);
    }

    private class MessageDefBuilder {
        private final StartElement startElement;
        private final LinkedHashMap<String,StartElement> fieldRefs = new LinkedHashMap<String, StartElement>();
        private final String msgType;

        private MessageDefBuilder(StartElement startElement) {
            this.startElement = startElement;
            msgType = StAXHelper.stringAttribute(startElement, "msgtype");
        }

        void addFieldRef(StartElement fieldRef) {
            String fieldName = StAXHelper.stringAttribute(fieldRef, "name");
            if (fieldRefs.containsKey(fieldName)) {
                throw new DictionaryParseException("Field '" + fieldName + "' already referenced! "
                        + getErrorLocation(fieldRef));
            }
            // NOTE: This will be resolved later.
            fieldRefs.put(fieldName, fieldRef);
        }

        public String getMsgType() {
            return msgType;
        }
    }

    private void valueDef(StartElement startElement) {
        // Add the value to the current set of values.
        if (currentField == null)
            throw new DictionaryParseException("No field definition! " + getErrorLocation(startElement));
        try {
            currentField.addValue(startElement);
        } catch (Exception e) {
            throw new DictionaryParseException("Unexpected error " +
                    e.getMessage() + " " + getErrorLocation(startElement));
        }
    }
}
