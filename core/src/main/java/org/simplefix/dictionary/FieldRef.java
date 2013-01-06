package org.simplefix.dictionary;

/**
 * A reference to a FIX tag definition.
 * <br>
 * User: josh
 * Date: 1/6/13
 * Time: 10:05 AM
 */
public class FieldRef {
    private final FieldDef fieldDef;
    private final boolean required;

    public FieldRef(FieldDef fieldDef, boolean required) {
        if (fieldDef == null) throw new IllegalArgumentException("fieldDef cannot be null!");
        this.fieldDef = fieldDef;
        this.required = required;
    }

    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldRef)) return false;

        FieldRef fieldRef = (FieldRef) o;

        if (fieldDef != null ? !fieldDef.equals(fieldRef.fieldDef) : fieldRef.fieldDef != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return fieldDef != null ? fieldDef.hashCode() : 0;
    }


    @Override
    public String toString() {
        return "FieldRef{" +
                "fieldDef=" + fieldDef +
                ", required=" + required +
                '}';
    }
}
