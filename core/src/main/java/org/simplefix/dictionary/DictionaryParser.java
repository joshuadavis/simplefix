package org.simplefix.dictionary;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.sun.istack.internal.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
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
    private static final String COMPONENT = "component";
    private static final String COMPONENTS = "components";
    private static final String MESSAGE = "message";
    private static final String MESSAGES = "messages";

    static class NamedEntry {
        private final Element domElement;

        NamedEntry(Element domElement) {
            this.domElement = domElement;
        }

        String getName() {
            return domElement.getAttribute("name");
        }
    }

    static class FieldEntry extends NamedEntry {
        FieldEntry(Element domElement) {
            super(domElement);
        }
    }

    static class ComponentEntry extends NamedEntry {
        ComponentEntry(Element domElement) {
            super(domElement);
        }
    }

    static class MessageEntry extends NamedEntry {
        MessageEntry(Element domElement) {
            super(domElement);
        }
    }

    private static int twoLevel_forEach(Element root, String name1, String name2,
                                        Function<Element, Boolean> function) {
        int processed = 0;
        final NodeList outerList = root.getChildNodes();
        for (int i = 0; i < outerList.getLength(); i++) {
            final Element outer = namedElement(outerList, i, name1);
            if (outer == null)
                continue;
            final NodeList innerList = outer.getChildNodes();
            for (int j = 0; j < innerList.getLength(); j++) {
                final Element inner = namedElement(innerList, j, name2);
                if (inner == null)
                    continue;
                Boolean flag = function.apply(inner);
                if (flag != null && flag) processed++;
            }
        }
        return processed;
    }

    private static Element namedElement(NodeList list, int index, String tagName) {
        Node n = list.item(index);
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element) n;
            if (tagName.equals(e.getTagName()))
                return e;
        }
        return null;
    }

    public static Dictionary parseXML(URL url) throws DictionaryParseException {
        final Document document = domParse(url);

        Element rootElement = document.getDocumentElement();
        if (!FIX.equals(rootElement.getTagName()))
            throw new DictionaryParseException("Unexpected root element tag name: '" +
                    rootElement.getTagName() + "'");

        // Index all the field definitions by name.   Parse the field definitions.
        final Map<String, FieldEntry> fieldsByName = Maps.newHashMap();
        int fieldCount = twoLevel_forEach(rootElement, FIELDS, FIELD, new Function<Element, Boolean>() {
            public Boolean apply(@Nullable Element element) {
                FieldEntry entry = new FieldEntry(element);
                String name = entry.getName();
                if (fieldsByName.containsKey(name))
                    throw new DictionaryParseException("Duplicate field definition: " + name);
                fieldsByName.put(name, entry);
                return true;
            }
        });
        log.info("parseXML() : " + fieldCount + " fields parsed.");

        // Index all the component definitions by name.
        final Map<String, ComponentEntry> componentsByName = Maps.newHashMap();
        int componentCount = twoLevel_forEach(rootElement, COMPONENTS, COMPONENT, new Function<Element, Boolean>() {
            public Boolean apply(@Nullable Element element) {
                ComponentEntry entry = new ComponentEntry(element);
                String name = entry.getName();
                if (componentsByName.containsKey(name))
                    throw new DictionaryParseException("Duplicate component definition: " + name);
                componentsByName.put(name, entry);
                return true;
            }
        });
        log.info("parseXML() : " + componentCount + " components parsed.");

        // Index all the message types by name.
        final Map<String, MessageEntry> messagesByName = Maps.newHashMap();
        int messageCount = twoLevel_forEach(rootElement, MESSAGES, MESSAGE, new Function<Element, Boolean>() {
            public Boolean apply(@Nullable Element element) {
                MessageEntry entry = new MessageEntry(element);
                String name = entry.getName();
                if (messagesByName.containsKey(name))
                    throw new DictionaryParseException("Duplicate message type definition: " + name);
                messagesByName.put(name, entry);
                return true;
            }
        });
        log.info("parseXML() : " + messageCount + " message types parsed.");

        // Parse all the message types link the fields up by name.

        return new Dictionary();
    }

    private static Document domParse(URL url) {
        try {
            // Use reg'ler old DOM parsing.
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(url.toString());
        } catch (Exception e) {
            final String msg = "Unable to parse " + url + " due to : " + e;
            log.error(msg, e);
            throw new DictionaryParseException(msg, e);
        }
    }
}
