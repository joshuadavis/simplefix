package org.simplefix.dictionary.test;

import org.junit.Test;
import org.simplefix.dictionary.*;
import org.simplefix.dictionary.xml.DictionaryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static junit.framework.Assert.*;

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


    @Test()
    public void checkFix44() throws Exception {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("FIX44.xml");
        Dictionary dictionary = DictionaryParser.parseXML(resource);
        assertNotNull(dictionary);
        FieldDef fieldDef = dictionary.getFieldDef(1);
        assertNotNull(fieldDef);
        FieldDef fieldDef1 = dictionary.getFieldDef("Account");
        assertNotNull(fieldDef1);
        assertEquals(fieldDef, fieldDef1);

        MessageType heartbeat = dictionary.getMessageType("0");
        assertNotNull(heartbeat);
        assertFalse(heartbeat.isApplicationMessage());
        assertEquals(1,heartbeat.getNumberOfTags());
        final FieldRef testReqID = heartbeat.fieldRefs().next();
        assertEquals(dictionary.getFieldDef(112), testReqID.getFieldDef());
        assertFalse(testReqID.isRequired());
        log.info("heartbeat message type=" + heartbeat);
    }

    @Test(expected = DictionaryParseException.class)
    public void checkDupField() throws Exception {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("FIX44-bad1.xml");
        log.info("NOTE: There will be an ERROR level log message here.   PLEASE IGNORE.");
        DictionaryParser.parseXML(resource);
    }
}
