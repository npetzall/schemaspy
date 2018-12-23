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

import java.util.Objects;

/**
 * Base class to indicate that there was problem with how SchemaSpy was configured / used.
 *
 * @author John Currier
 * @author Nils Petzaell
 */
public class InvalidConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String paramName;
    private String paramValue = "";

    /**
     * When a message is sufficient
     *
     * @param msg
     */
    public InvalidConfigurationException(String msg) {
        super(msg);
    }

    /**
     * When there's an associated root cause.
     * The resultant msg will be a combination of <code>msg</code> and cause's <code>msg</code>.
     *
     * @param msg
     * @param cause
     */
    public InvalidConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * When there are no details other than the root cause
     *
     * @param cause
     */
    public InvalidConfigurationException(Throwable cause) {
        super(cause);
    }

    public InvalidConfigurationException setParamName(String paramName) {
        this.paramName = paramName;
        return this;
    }

    public String getParamName() {
        return paramName;
    }

    public InvalidConfigurationException setParamValue(String paramValue) {
        if (Objects.nonNull(paramValue))
            this.paramValue = paramValue;
        return this;
    }

    public String getParamValue() {
        return paramValue;
    }
}
