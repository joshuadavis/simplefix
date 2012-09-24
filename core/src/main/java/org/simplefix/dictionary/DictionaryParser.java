package org.simplefix.dictionary;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
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

    private static final String FIELDS = "fields";
    private static final String FIELD = "field";
    private static final String FIX = "fix";
    private static final String VALUE = "value";
    private static final String PATHSEP = "/";
    private static final String FIELD_DEF_PATH = FIX + PATHSEP + FIELDS + PATHSEP + FIELD;
    private static final String VALUE_DEF_PATH = FIELD_DEF_PATH + PATHSEP + VALUE;
    private static final String COMPONENT = "component";
    private static final String COMPONENTS = "components";
    private static final String MESSAGE = "message";
    private static final String MESSAGES = "messages";

    // Parser state
    private final List<Elem> path = Lists.newArrayList();
    private FieldDefBuilder currentField = null;
    private final Map<String, Dictionary.FieldDef> fieldsByName = Maps.newHashMap();

    public static Dictionary parseXML(URL url) throws DictionaryParseException {
        XMLEventReader eventReader = createXMLEventReader(url);
        try {
            return new DictionaryParser().doParse(eventReader);
        } catch (XMLStreamException e) {
            throw new DictionaryParseException("Unable to read " + url + " due to " + e, e);
        }
    }

    private static int intAttribute(StartElement startElement, String attributeName) {
        return Integer.parseInt(stringAttribute(startElement, attributeName));
    }

    private static String stringAttribute(StartElement startElement, String attributeName) {
        return requireAttribute(startElement, attributeName).getValue();
    }

    private static Attribute requireAttribute(StartElement startElement, String attributeName) {
        Attribute attribute = startElement.getAttributeByName(new QName(attributeName));
        if (attribute == null)
            throw new DictionaryParseException("Required attribute '" + attributeName +
                    "' missing on " + startElement + " at " + startElement.getLocation());
        return attribute;
    }

    private static String elementName(StartElement startElement) {
        return startElement.getName().getLocalPart();
    }

    private static class Elem {
        private final StartElement startElement;
        private final String path;

        private Elem(Elem parent, StartElement startElement) {
            this.startElement = startElement;
            String localPart = elementName(startElement);
            this.path = (parent == null) ? localPart : parent.getPath() + PATHSEP + localPart;
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

        public Location getStartLocation() {
            return startElement.getLocation();
        }
    }

    private static class FieldDefBuilder {
        private final int tag;
        private final String name;
        private final Map<String, String> values = Maps.newHashMap();
        private final Dictionary.FieldType type = null;

        private FieldDefBuilder(StartElement startElement) {
            tag = intAttribute(startElement,"number");
            name = stringAttribute(startElement,"name");
        }


        public Dictionary.FieldDef createFieldDef() {
            return new Dictionary.FieldDef(tag,name,type,values);
        }

        public void addValue(StartElement startElement) {
            String key = stringAttribute(startElement,"enum");
            String name = stringAttribute(startElement,"description");
            if (values.containsKey(key))
                throw new DictionaryParseException("Duplicate value " + startElement +
                        " at " + startElement.getLocation());
            values.put(key,name);
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

        // Parse all the message types link the fields up by name.

        return new Dictionary(fieldsByName);
    }

    private void endElement(XMLEvent event) {
        EndElement endElement = event.asEndElement();
        Elem e = path.remove(path.size() - 1);
        log.info("path=" + e.getPath());
        if (FIELD_DEF_PATH.equals(e.getPath())) {
            fieldDef(endElement, e);
        } else if (VALUE_DEF_PATH.equals(e.getPath())) {
        }
    }

    private void fieldDef(EndElement endElement, Elem e) {
        // We're done with a field definition.
        if (currentField == null)
            throw new DictionaryParseException("No field definition! at " + endElement.getLocation());
        Dictionary.FieldDef fieldDef = currentField.createFieldDef();
        if (fieldsByName.containsKey(fieldDef.getName())) {
            throw new DictionaryParseException("Duplicate field '" + fieldDef.getName() + "' at " +
                    e.getStartLocation());
        }
        currentField = null;
    }

    private void startElement(XMLEvent event) {
        StartElement startElement = event.asStartElement();
        Elem elem = new Elem(
                path.isEmpty() ? null : getCurrent(path),
                startElement);
        path.add(elem);
        if (FIELD_DEF_PATH.equals(elem.getPath())) {
            currentField = new FieldDefBuilder(startElement);
        }
        else if (VALUE_DEF_PATH.equals(elem.getPath())) {
            valueDef(startElement);
        }
    }

    private void valueDef(StartElement startElement) {
        // Add the value to the current set of values.
        if (currentField == null)
            throw new DictionaryParseException("No field definition! at " + startElement.getLocation());
        try {
            currentField.addValue(startElement);
        } catch (Exception e) {
            throw new DictionaryParseException("Unexpected error " + e.getMessage() + " at "
                    + startElement.getLocation());
        }
    }

    private static Elem getCurrent(List<Elem> path) {
        return path.get(path.size() - 1);
    }

    private static XMLEventReader createXMLEventReader(URL url) {
        try {
            XMLInputFactory f = XMLInputFactory.newInstance();
            f.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            url.openStream()));
            return f.createXMLEventReader(new StreamSource(reader));
        } catch (Exception e) {
            throw new DictionaryParseException("Unable to read " + url + " due to " + e, e);
        }
    }

}
