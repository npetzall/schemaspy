/*
 * Copyright (C) 2018 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.input.dbms.exceptions;

/**
 * Indicates that we couldn't connect to the database
 *
 * @author John Currier
 */
public class ConnectionFailure extends RuntimeException {
    private static final long serialVersionUID = 1L;
    /**
     * When a message is sufficient
     *
     * @param msg
     */
    public ConnectionFailure(String msg) {
        super(msg);
    }

    /**
     * When there's an associated root cause.
     * The resultant msg will be a combination of <code>msg</code> and cause's <code>msg</code>.
     *
     * @param msg
     * @param cause
     */
    public ConnectionFailure(String msg, Throwable cause) {
        super(msg + " " + cause.getMessage(), cause);
    }

    /**
     * When there are no details other than the root cause
     *
     * @param cause
     */
    public ConnectionFailure(Throwable cause) {
        super(cause);
    }
}
