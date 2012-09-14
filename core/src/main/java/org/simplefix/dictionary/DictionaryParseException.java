package org.simplefix.dictionary;

/**
 * Thrown when the dictionary cannot be parsed.
 * <br>
 * User: josh
 * Date: 9/14/12
 * Time: 1:09 PM
 */
public class DictionaryParseException extends RuntimeException {
    public DictionaryParseException(String message) {
        super(message);
    }

    public DictionaryParseException(String msg, Exception e) {
        super(msg,e);
    }
}
