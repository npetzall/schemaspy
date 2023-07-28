/*
 * Copyright (C) 2004-2010 John Currier
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Nils Petzaell
 *
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy.util;

import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Configuration of a specific type of database (as specified by -t)
 *
 * @author John Currier
 * @author Wojciech Kasa
 * @author Thomas Traude
 * @author Nils Petzaell
 */
public class DbSpecificConfig {

    private static final Pattern OPTION_PATTER = Pattern.compile("<([a-zA-Z0-9.\\-_]+)>");
    private static final String DUMP_FORMAT = "      -%s   \t\t%s";

    private final String dbType;
    private final Properties properties;

    public DbSpecificConfig(String dbType, Properties props) {
        this.dbType = dbType;
        this.properties = props;
    }

    /**
     * Returns a {@link List} of {@link DbSpecificOption}s that are applicable to the
     * specified database type.
     *
     * @return
     */
    public List<DbSpecificOption> getOptions() {
        Set<String> optionsFound = findOptions(properties.getProperty("connectionSpec"));
        return optionsFound
            .stream()
            .map(optionName ->
                     new DbSpecificOption(
                         optionName,
                         properties.getProperty(optionName)
                     )
            )
            .collect(Collectors.toList());
    }

    private static Set<String> findOptions(String connectionSpec) {
        Set<String> optionsFound = new LinkedHashSet<>();
        Matcher matcher = OPTION_PATTER.matcher(connectionSpec);
        while (matcher.find()) {
            optionsFound.add(matcher.group(1));
        }
        return optionsFound;
    }

    /**
     * Dump usage details associated with the associated type of database
     */
    public void dumpUsage(Logger logger) {
        logger.info("   {} (-t {})", this, dbType);
        getOptions().stream().flatMap(option -> {
            if ("hostOptionalPort".equals(option.getName())) {
                return Stream.of(
                    String.format(DUMP_FORMAT, "host", "host of database, may contain port"),
                    String.format(DUMP_FORMAT, "port", "optional port if not default")
                );
            } else {
                return Stream.of(
                    String.format(DUMP_FORMAT, option.getName(), getDescription(option))
                );
            }
        }).forEach(logger::info);
    }

    private static String getDescription(DbSpecificOption option) {
        return Objects.isNull(option.getDescription()) ? "" : option.getDescription();
    }

    /**
     * Return description of the associated type of database
     */
    @Override
    public String toString() {
        return properties.getProperty("description");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof DbSpecificConfig) {
            DbSpecificConfig that = (DbSpecificConfig) o;
            return dbType.equals(that.dbType) && properties.equals(that.properties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dbType, properties);
    }
}
