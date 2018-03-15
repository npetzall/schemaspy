package org.schemaspy.input.dbms.exceptions;

public class MissingArgumentException extends RuntimeException {
    public MissingArgumentException(String message) {
        super(message);
    }
}
