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
        PathEvent start = path.remove(path.size() - 1);
        PathEvent parent = getCurrentParent();
        return new PathEvent(parent,event,start);
    }

    private PathEvent startElement(XMLEvent event) {
        PathEvent parent = getCurrentParent();
        StartElement startElement = event.asStartElement();
        // Add this start element to the path.
        PathEvent start = new PathEvent(parent,startElement);
        path.add(start);
        return start;
    }

    private PathEvent getCurrentParent() {
        return path.isEmpty() ? null : path.get(path.size() - 1);
    }

    private PathEvent reset(XMLEvent event) {
        path.clear();
        return new PathEvent(null,event);
    }
}
