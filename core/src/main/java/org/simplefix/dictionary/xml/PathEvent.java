package org.simplefix.dictionary.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.Writer;

/**
* A StAX XML event, with path information.
* <br>
* User: josh
* Date: 1/6/13
* Time: 11:27 AM
*/
public class PathEvent implements XMLEvent {
    private final XMLEvent delegate;
    private final PathEvent parent;
    private final PathEvent start;
    private final String name;
    private final String path;

    public PathEvent(PathEvent parent, XMLEvent delegate) {
        this(parent,delegate,null);
    }

    public PathEvent(PathEvent parent, XMLEvent event, PathEvent start) {
        this.parent = parent;
        this.delegate = event;
        this.start = start;
        name = (event.isStartElement() ?
                StAXHelper.elementName(event.asStartElement()) :
                (start != null ? start.getName() : ""));
        path = (parent == null ? "" : parent.getPath() + "/") + name;
    }

    public PathEvent getParent() {
        return parent;
    }

    public PathEvent getStart() {
        return start;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public int getEventType() {
        return delegate.getEventType();
    }

    public Location getLocation() {
        return delegate.getLocation();
    }

    public boolean isStartElement() {
        return delegate.isStartElement();
    }

    public boolean isAttribute() {
        return delegate.isAttribute();
    }

    public boolean isNamespace() {
        return delegate.isNamespace();
    }

    public boolean isEndElement() {
        return delegate.isEndElement();
    }

    public boolean isEntityReference() {
        return delegate.isEntityReference();
    }

    public boolean isProcessingInstruction() {
        return delegate.isProcessingInstruction();
    }

    public boolean isCharacters() {
        return delegate.isCharacters();
    }

    public boolean isStartDocument() {
        return delegate.isStartDocument();
    }

    public boolean isEndDocument() {
        return delegate.isEndDocument();
    }

    public StartElement asStartElement() {
        return delegate.asStartElement();
    }

    public EndElement asEndElement() {
        return delegate.asEndElement();
    }

    public Characters asCharacters() {
        return delegate.asCharacters();
    }

    public QName getSchemaType() {
        return delegate.getSchemaType();
    }

    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        delegate.writeAsEncodedUnicode(writer);
    }

    @Override
    public String toString() {
        return "PathEvent{" +
                "path=" + path +
                ", delegate=" + delegate +
                ", parent=" + parent +
                ", start=" + start +
                '}';
    }
}
