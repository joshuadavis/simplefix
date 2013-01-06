package org.simplefix.dictionary.xml;

import org.simplefix.dictionary.DictionaryParseException;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * StAX helper methods.
 * <br>
 * User: josh
 * Date: 1/6/13
 * Time: 7:52 AM
 */
public class StAXHelper {
    public static XMLEventReader createXMLEventReader(URL url) {
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

    public static XMLEventReader createXMLEventReaderForResource(String name) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(name);
        return createXMLEventReader(url);
    }

    static int intAttribute(StartElement startElement, String attributeName) {
        return Integer.parseInt(stringAttribute(startElement, attributeName));
    }

    static String stringAttribute(StartElement startElement, String attributeName) {
        return requireAttribute(startElement, attributeName).getValue();
    }

    static Attribute requireAttribute(StartElement startElement, String attributeName) {
        Attribute attribute = getAttribute(startElement, attributeName);
        if (attribute == null)
            throw new DictionaryParseException("Required attribute '" + attributeName +
                    "' missing on " + startElement + getLocationString(startElement));
        return attribute;
    }

    static Attribute getAttribute(StartElement startElement, String attributeName) {
        return startElement.getAttributeByName(new QName(attributeName));
    }

    static String getAttributeValue(StartElement startElement, String attributeName) {
        Attribute attribute = getAttribute(startElement, attributeName);
        return attribute == null ? null : attribute.getValue();
    }

    static String elementName(StartElement startElement) {
        return startElement.getName().getLocalPart();
    }

    static String getLocationString(XMLEvent event) {
        final Location location = event.getLocation();
        return " at line " + location.getLineNumber() +
                ", column " + location.getColumnNumber();
    }

    public static boolean booleanAttribute(StartElement element, String name) {
        String value = getAttributeValue(element, name);
        return "Y".equalsIgnoreCase(value) || "true".equals(value);
    }
}
