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
package org.schemaspy.input.dbms;

import java.util.Objects;

/**
 * @author John Currier
 * @author Thomas Traude
 * @author Nils Petzaell
 */
public final class DbSpecificOption {
    private final String name;
    private String value;
    private final String description;

    public DbSpecificOption(String name, String description) {
        this.name = Objects.requireNonNull(name);
        this.description = description;
    }

    public String getName() {
        return name;
    }

    //TODO This method may return null. Consider changing the return type to Optional<String> and return Optional.empty instead of null
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value.replaceAll("\\\\", "/");
    }

    //TODO This method may return null. Consider changing the return type to Optional<String> and return Optional.empty instead of null
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " name: '" + getName() + "' value: '" + getValue() + "' description: '" + getDescription() + "'";
    }
}

