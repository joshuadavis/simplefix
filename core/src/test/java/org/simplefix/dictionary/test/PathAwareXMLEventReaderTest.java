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

/**
 * Test of the XMLEventReader helper class PathAwareXMLEventReder
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
        while (pathAwareReader.hasNext()) {
            PathEvent event = pathAwareReader.nextEvent();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    Assert.assertTrue(event.getPath().startsWith("fix"));
                    break;
            }

        }
    }
}
