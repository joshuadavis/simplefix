package org.simplefix.dictionary.xml;

import com.google.common.collect.Lists;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.List;

/**
 * Decorates StAX XMLEventReader with path-to-parent knowledge.
 * <br>
 * User: josh
 * Date: 1/6/13
 * Time: 11:17 AM
 */
public class PathAwareXMLEventReader {

    /**
     * The current path of elements, up to the document root.
     */
    private final List<PathEvent> path = Lists.newArrayList();

    private int level = 0;

    private final XMLEventReader reader;

    public boolean hasNext() {
        return reader.hasNext();
    }

    public PathAwareXMLEventReader(XMLEventReader reader) {
        this.reader = reader;
    }

    public PathEvent nextEvent() throws XMLStreamException {
        XMLEvent event = reader.nextEvent();
        switch (event.getEventType()) {
            case XMLStreamConstants.START_DOCUMENT:
                return reset(event);
            case XMLStreamConstants.END_DOCUMENT:
                return reset(event);
            case XMLStreamConstants.START_ELEMENT:
                return startElement(event);
            case XMLStreamConstants.END_ELEMENT:
                return endElement(event);
            default:
                return new PathEvent(getCurrentParent(),event);
        }
    }

    private PathEvent endElement(XMLEvent event) {
        level--;
        PathEvent start = path.remove(level);
        PathEvent parent = getCurrentParent();
        return new PathEvent(parent,event,start);
    }

    private PathEvent startElement(XMLEvent event) {
        PathEvent parent = getCurrentParent();
        StartElement startElement = event.asStartElement();
        // Add this start element to the path.
        PathEvent start = new PathEvent(parent,startElement);
        path.add(start);
        level++;
        return start;
    }

    private PathEvent getCurrentParent() {
        return level == 0 ? null : path.get(level - 1);
    }

    private PathEvent reset(XMLEvent event) {
        path.clear();
        level = 0;
        return new PathEvent(null,event);
    }
}
