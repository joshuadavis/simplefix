package org.yajul.simplefix.test;

import org.junit.Test;
import org.simplefix.data_dictionary.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.URL;
import static junit.framework.Assert.*;

/**
 * TODO: Add class level comments
 * <br/>
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 9/12/12
 * Time: 7:19 AM
 */
public class DataDictionaryParserTest {
    @Test
    public void parseFIX44() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Fix.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        URL resource = Thread.currentThread().getContextClassLoader().getResource("FIX44.xml");
        Fix dd = (Fix) unmarshaller.unmarshal(resource);
        assertNotNull(dd);
        System.out.println("dd=" + dd);
    }
}
