package org.simplefix.dictionary;

import java.util.LinkedHashMap;

/**
 * FIX repeating group definition.
 * <br>
 * User: josh
 * Date: 1/6/13
 * Time: 11:05 AM
 */
public class RepeatingGroupDef extends FieldRefGroup {
    private final FieldRef countField;

    public RepeatingGroupDef(LinkedHashMap<Integer, FieldRef> fieldRefsByTag, FieldRef countField) {
        super(fieldRefsByTag);
        this.countField = countField;
    }
}
