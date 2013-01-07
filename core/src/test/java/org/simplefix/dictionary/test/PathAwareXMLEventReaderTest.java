package org.simplefix.dictionary.test;

import org.junit.Assert;
import org.junit.Test;
import org.simplefix.dictionary.xml.PathAwareXMLEventReader;
import org.simplefix.dictionary.xml.PathEvent;
import org.simplefix.dictionary.xml.StAXHelper;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test of the XMLEventReader helper class PathAwareXMLEventReader
 * <br>
 * User: josh
 * Date: 1/6/13
 * Time: 11:22 AM
 */
public class PathAwareXMLEventReaderTest {

    @Test
    public void testPathAwareReader() throws XMLStreamException {
        XMLEventReader reader = StAXHelper.createXMLEventReaderForResource("FIX44.xml");
        PathAwareXMLEventReader pathAwareReader = new PathAwareXMLEventReader(reader);
        int header = 0;
        int messages = 0;
        int messageCount = 0;
        while (pathAwareReader.hasNext()) {
            PathEvent event = pathAwareReader.nextEvent();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    if ("fix/messages".equals(event.getPath())) {
                        messages++;
                    } else if ("fix/header".equals(event.getPath())) {
                        header++;
                    } else if ("fix/messages/message".equals(event.getPath())) {
                        messageCount++;
                        event.setContext("this is message " + StAXHelper.stringAttribute(
                                event.asStartElement(),"name"));
                    } else if ("fix/messages/message/field".equals(event.getPath())) {
                        assertTrue(event.getParent().getContextAs(String.class).startsWith("this is message"));
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    assertTrue(event.getPath().startsWith("fix"));
                    if ("fix/messages/message".equals(event.getPath())) {
                        assertTrue(event.getStart().getContextAs(String.class).startsWith("this is message"));
                    }
                    break;
            }
        } // while
        assertEquals(1, header);
        assertEquals(1, messages);
        assertEquals(92, messageCount);
    }
}
