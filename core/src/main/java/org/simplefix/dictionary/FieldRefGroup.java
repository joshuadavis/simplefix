package org.simplefix.dictionary;

import java.util.LinkedHashMap;

/**
 * Defines a group of FIX fields (header, trailer, message type, or component).
 * <br>
 * User: josh
 * Date: 1/6/13
 * Time: 10:08 AM
 */
public class FieldRefGroup {
    private final LinkedHashMap<Integer,FieldRef> fieldRefsByTag;

    public FieldRefGroup(LinkedHashMap<Integer, FieldRef> fieldRefsByTag) {
        this.fieldRefsByTag = fieldRefsByTag;
    }

    @Override
    public String toString() {
        return "fieldRefsByTag=" + fieldRefsByTag ;
    }
}
