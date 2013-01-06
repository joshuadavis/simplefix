package org.simplefix.dictionary;

import java.util.LinkedHashMap;

/**
 * Represents a FIX message type definition.
 * <br>
 * User: josh
 * Date: 1/6/13
 * Time: 10:04 AM
 */
public class MessageType extends FieldRefGroup {
    private final String msgType;

    public MessageType(String msgType, LinkedHashMap<Integer, FieldRef> fieldRefsByTag) {
        super(fieldRefsByTag);
        this.msgType = msgType;
    }

    public String getMsgType() {
        return msgType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageType)) return false;

        MessageType that = (MessageType) o;

        if (msgType != null ? !msgType.equals(that.msgType) : that.msgType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return msgType != null ? msgType.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "MessageType{" +
                "msgType='" + msgType + '\'' +
                "," + super.toString() +
                '}';
    }
}
