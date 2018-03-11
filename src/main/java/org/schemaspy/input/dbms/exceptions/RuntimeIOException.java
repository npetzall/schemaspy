/*
 * Copyright (C) 2017 Nils Petzaell
 */
package org.schemaspy.input.dbms.exceptions;

/**
 * @author Nils Petzaell
 */
public class RuntimeIOException extends RuntimeException {
    public RuntimeIOException(String message, Throwable cause) {
        super(message, cause);
    }
}
