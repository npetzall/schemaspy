/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010 John Currier
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
package org.schemaspy.view;

import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.util.CaseInsensitiveMap;
import org.schemaspy.util.HtmlEncoder;

import java.util.*;

/**
 * Default implementation of {@link SqlFormatter}
 *
 * @author John Currier
 */
public class DefaultSqlFormatter implements SqlFormatter {
    private Set<String> keywords;
    private Map<String, Table> tablesByPossibleNames;
    private static String TOKENS = " \t\n\r\f()<>|,";

    /**
     * Return a HTML-formatted representation of the specified SQL.
     *
     * @param sql SQL to be formatted
     * @param db Database
     * @return HTML-formatted representation of the specified SQL
     */
    public String format(String sql, Database db, Set<Table> references) {
        StringBuilder formatted = new StringBuilder(sql.length() * 2);

        boolean alreadyFormatted = sql.contains("\n") || sql.contains("\r");
        if (alreadyFormatted)
        {
            // apparently already formatted, so dump it as is
            formatted.append("<div class='viewDefinition preFormatted'>");

            int len = sql.length();
            for (int i = 0; i < len; i++) {
                char ch = sql.charAt(i);

                // encode everything except whitespace
                if (Character.isWhitespace(ch)) {
                    formatted.append(ch);
                } else {
                    formatted.append(HtmlEncoder.encodeToken(ch));
                }
            }
        }
        else
        {
            formatted.append("  <div class='viewDefinition'>");
            @SuppressWarnings("hiding")
            Set<String> keywords = getKeywords(db);
            StringTokenizer tokenizer = new StringTokenizer(sql, TOKENS, true);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (keywords.contains(token.toUpperCase())) {
                    formatted.append("<b>");
                    formatted.append(token);
                    formatted.append("</b>");
                } else {
                    formatted.append(HtmlEncoder.encodeToken(token));
                }
            }
        }

        formatted.append("</div>");

        references.addAll(getReferencedTables(sql, db));

        return formatted.toString();
    }

    /**
     * Returns a {@link Set} of tables/views that are possibly referenced
     * by the specified SQL.
     *
     * @param sql
     * @param db
     * @return
     */
    public Set<Table> getReferencedTables(String sql, Database db) {
        Set<Table> referenced = new HashSet<Table>();

        Map<String, Table> tables = getTableMap(db);
        @SuppressWarnings("hiding")
        Set<String> keywords = getKeywords(db);

        StringTokenizer tokenizer = new StringTokenizer(sql, TOKENS, true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!keywords.contains(token.toUpperCase())) {
                Table t = tables.get(token);

                if (t == null) {
                    int lastDot = token.lastIndexOf('.');
                    if (lastDot != -1) {
                        t = tables.get(token.substring(0, lastDot));
                    }
                }

                if (t != null) {
                    referenced.add(t);
                }
            }
        }

        return referenced;
    }

    /**
     * Returns a {@link Map} of all tables/views in the database
     * keyed by several possible ways to refer to the table.
     *
     * @param db
     * @return
     */
    protected Map<String, Table> getTableMap(Database db)
    {
        if (tablesByPossibleNames == null)
        {
            tablesByPossibleNames = new CaseInsensitiveMap<Table>();

            tablesByPossibleNames.putAll(getTableMap(db.getTables()));
            tablesByPossibleNames.putAll(getTableMap(db.getViews()));
        }

        return tablesByPossibleNames;
    }

    /**
     * Returns a {@link Map} of the specified tables/views
     * keyed by several possible ways to refer to the table.
     *
     * @param tables
     * @return
     */
    protected Map<String, Table> getTableMap(Collection<? extends Table> tables) {
        Map<String, Table> map = new CaseInsensitiveMap<Table>();
        for (Table t : tables) {
            String name = t.getName();
            String container = t.getContainer();

            map.put(name, t);
            map.put("`" + name + "`", t);
            map.put("'" + name + "'", t);
            map.put("\"" + name + "\"", t);
            map.put(container + "." + name, t);
            map.put("`" + container + "`.`" + name + "`", t);
            map.put("'" + container + "'.'" + name + "'", t);
            map.put("\"" + container + "\".\"" + name + "\"", t);
            map.put("`" + container + '.' + name + "`", t);
            map.put("'" + container + '.' + name + "'", t);
            map.put("\"" + container + '.' + name + "\"", t);
        }

        return map;
    }

    /**
     * @param database
     * @return
     */
    public Set<String> getKeywords(Database database) {
        if (keywords == null) {
            keywords = new HashSet<>();
            keywords.addAll(database.getSqlKeywords());
            keywords.addAll(database.getSystemFunctions());
            keywords.addAll(database.getNumericFunctions());
            keywords.addAll(database.getStringFunctions());
            keywords.addAll(database.getTimeDateFunctions());
        }
        return keywords;
    }
}
