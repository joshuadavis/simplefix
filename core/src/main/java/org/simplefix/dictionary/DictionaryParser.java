package org.simplefix.dictionary;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.*;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.simplefix.dictionary.StAXHelper.stringAttribute;

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
     * The current path of elements, up to the document root.
     */
    private final List<Elem> path;
    /**
     * Builder for the current field definition.
     */
    private FieldDefBuilder currentField;

    private MessageDefBuilder currentMessage;

    private final Map<String,MessageDefBuilder> messageTypes;

    private final DictionaryBuilder builder;

    private final String documentName;


    public DictionaryParser(String documentName) {
        builder = new DictionaryBuilder();
        currentField = null;
        path = Lists.newArrayList();
        messageTypes = Maps.newHashMap();
        this.documentName = documentName;
    }

    public static Dictionary parseXML(URL url) throws DictionaryParseException {
        XMLEventReader eventReader = StAXHelper.createXMLEventReader(url);
        try {
            return new DictionaryParser(url.toString()).doParse(eventReader);
        } catch (XMLStreamException e) {
            throw new DictionaryParseException("Unable to read " + url + " due to " + e, e);
        } catch (DictionaryParseException dpe) {
            log.error("Unable to read " + url + " due to " + dpe, dpe);
            throw dpe;
        }
    }

    private static class Elem {
        private final StartElement startElement;
        private final String path;

        private Elem(Elem parent, StartElement startElement) {
            this.startElement = startElement;
            String localPart = StAXHelper.elementName(startElement);
            this.path = (parent == null) ? localPart : parent.getPath() + "/" + localPart;
        }

        @Override
        public String toString() {
            return "Elem{" +
                    "startElement=" + startElement +
                    '}';
        }

        public String getPath() {
            return path;
        }
    }

    private String getErrorLocation(XMLEvent event) {
        return StAXHelper.getLocationString(documentName, event);
    }

    private class FieldDefBuilder {
        private final int tag;
        private final String name;
        private final String type;
        private final Map<String, String> values = Maps.newHashMap();
        private final ValueType valueType;

        private FieldDefBuilder(StartElement startElement) {
            tag = StAXHelper.intAttribute(startElement, "number");
            name = stringAttribute(startElement, "name");
            type = stringAttribute(startElement, "type");
            valueType = builder.mapToValueType(type);
        }

        public FieldDef createFieldDef() {
            return new FieldDef(tag, name, type, valueType, values);
        }

        public void addValue(StartElement startElement) {
            String key = stringAttribute(startElement, "enum");
            String name = stringAttribute(startElement, "description");
            if (values.containsKey(key))
                throw new DictionaryParseException("Duplicate value " + startElement +
                        getErrorLocation(startElement));
            values.put(key, name);
        }
    }

    private Dictionary doParse(XMLEventReader eventReader) throws XMLStreamException {

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT:
                    path.clear();
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    path.clear();
                    break;
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
                String fieldName = stringAttribute(fieldRef,"name");
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
            MessageType messageType = new MessageType(msgType, refMap);
            builder.addMessageType(messageType);
        }
    }

    private void endElement(XMLEvent event) {
        EndElement endElement = event.asEndElement();
        Elem e = path.remove(path.size() - 1);
        final String pathString = e.getPath();
        if (pathString.startsWith("fix/fields")) {
            if ("fix/fields/field".equals(pathString)) {
                fieldDef(endElement, e);
            }
        } else if (pathString.startsWith("fix/messages")) {
            if ("fix/messages/message".equals(pathString)) {
                messageDef(endElement,e);
            }
        }
    }

    private void messageDef(EndElement endElement, Elem e) {
        if (messageTypes.containsKey(currentMessage.getMsgType())) {
            throw new DictionaryParseException("Duplicate message type '" + currentMessage.getMsgType() + "' " +
                    getErrorLocation(e.startElement));
        }
        messageTypes.put(currentMessage.getMsgType(), currentMessage);
    }

    private void fieldDef(EndElement endElement, Elem e) {
        // We're done with a field definition.
        if (currentField == null)
            throw new DictionaryParseException("No field definition! at " + endElement.getLocation());
        FieldDef fieldDef = currentField.createFieldDef();
        if (builder.containsFieldName(fieldDef.getName())) {
            throw new DictionaryParseException("Duplicate field '" + fieldDef.getName() + "' " +
                    getErrorLocation(e.startElement));
        }
        if (builder.containsTag(fieldDef.getTag())) {
            throw new DictionaryParseException("Duplicate tag '" + fieldDef.getTag() + "' at " +
                    getErrorLocation(e.startElement));
        }
        builder.addFieldDef(fieldDef);
        currentField = null;
    }

    private void startElement(XMLEvent event) {
        StartElement startElement = event.asStartElement();
        Elem elem = new Elem(
                path.isEmpty() ? null : getCurrent(path),
                startElement);
        path.add(elem);
        final String pathString = elem.getPath();
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
            msgType = stringAttribute(startElement,"msgtype");
        }

        void addFieldRef(StartElement fieldRef) {
            String fieldName = stringAttribute(fieldRef, "name");
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

    private static Elem getCurrent(List<Elem> path) {
        return path.get(path.size() - 1);
    }

}
