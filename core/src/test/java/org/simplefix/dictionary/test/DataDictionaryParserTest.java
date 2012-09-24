package org.simplefix.dictionary.test;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.simplefix.dictionary.DictionaryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Test the data dictionary parsing code.
 * <br/>
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 9/12/12
 * Time: 7:19 AM
 */
public class DataDictionaryParserTest {
    private static final Logger log = LoggerFactory.getLogger(DataDictionaryParserTest.class);

    @Test
    public void parseFIX44() throws Exception {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("FIX44.xml");
        DictionaryParser.parseXML(resource);
/*
        Dictionary dictionary = DictionaryParser.parseXML(resource);
        assertNotNull(dictionary);
*/
    }
}
