package org.simplefix.dictionary;

/**
* Represents a FIX field value type.
* <br>
* User: josh
* Date: 1/6/13
* Time: 8:04 AM
*/
public enum ValueType {
    /** No type, just a string. **/
    ANY,

    /** Java primitive types **/
    INTEGER,
    STRING,
    CHARACTER,
    DOUBLE,
    BOOLEAN,
}
