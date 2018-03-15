/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2014 John Currier
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
package org.schemaspy.model;

import org.schemaspy.model.xml.SchemaMeta;
import org.schemaspy.util.CaseInsensitiveMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Database {

    private final String databaseProduct;
    private final String databaseName ;
    private final Catalog catalog ;
    private final Schema schema;
    private final SchemaMeta schemaMeta;

    private final Set<String> sqlKeywords;
    private final Set<String> systemFunctions;
    private final Set<String> numericFunctions;
    private final Set<String> stringFunctions;
    private final Set<String> timeDateFunctions;

    private final Map<String, Table> tables = new CaseInsensitiveMap<>();
    private final Map<String, View> views = new CaseInsensitiveMap<>();
    private final Map<String, Table> remoteTables = new CaseInsensitiveMap<>(); // key: schema.tableName
    private final Map<String, Table> locals = new CombinedMap(tables, views);
    private final Map<String, Routine> routines = new CaseInsensitiveMap<>();

    public Database(
            String databaseProduct,
            String name,
            String catalog,
            String schema,
            SchemaMeta schemaMeta,
            Set<String> sqlKeywords,
            Set<String> systemFunctions,
            Set<String> numericFunctions,
            Set<String> stringFunctions,
            Set<String> timeDateFunctions
    ) {
        this.databaseProduct = databaseProduct;
        this.databaseName = name;
        this.catalog = new Catalog(catalog);
        this.schema = new Schema(schema);
        this.schemaMeta = schemaMeta;
        this.sqlKeywords = sqlKeywords;
        this.systemFunctions = systemFunctions;
        this.numericFunctions = numericFunctions;
        this.stringFunctions = stringFunctions;
        this.timeDateFunctions = timeDateFunctions;
    }

    public String getDatabaseProduct() {
        return databaseProduct;
    }

    public String getName() {
        return databaseName;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public Schema getSchema() {
        return schema;
    }

    public Set<String> getSqlKeywords() {
        return sqlKeywords;
    }

    public Set<String> getSystemFunctions() {
        return systemFunctions;
    }

    public Set<String> getNumericFunctions() {
        return numericFunctions;
    }

    public Set<String> getStringFunctions() {
        return stringFunctions;
    }

    public Set<String> getTimeDateFunctions() {
        return timeDateFunctions;
    }

    public Collection<Table> getTables() {
        return tables.values();
    }

    public Map<String, Table> getTablesMap() {
        return tables;
    }

    /**
     * Return a {@link Map} of all {@link Table}s keyed by their name.
     *
     * @return
     */
    public Map<String, Table> getTablesByName() {
        return tables;
    }

    public Map<String, Table> getLocals() {
        return locals;
    }

    public Collection<View> getViews() {
        return views.values();
    }

    public Map<String, View> getViewsMap() {
        return views;
    }

    public Collection<Table> getRemoteTables() {
        return remoteTables.values();
    }

    public Map<String, Table> getRemoteTablesMap() {
        return remoteTables;
    }

    public Collection<Routine> getRoutines() {
        return routines.values();
    }

    public Map<String, Routine> getRoutinesMap() {
        return routines;
    }

    public SchemaMeta getSchemaMeta() {
        return schemaMeta;
    }


    /**
     * Returns a 'key' that's used to identify a remote table
     * in the remoteTables map.
     *
     * @param cat
     * @param sch
     * @param table
     * @return
     */
    public String getRemoteTableKey(String cat, String sch, String table) {
        return Table.getFullName(getName(), cat, sch, table);
    }

    /**
     * A read-only map that treats both collections of Tables and Views as one
     * combined collection.
     * This is a bit strange, but it simplifies logic that otherwise treats
     * the two as if they were one collection.
     */
    private class CombinedMap implements Map<String, Table> {
        private final Map<String, ? extends Table> map1;
        private final Map<String, ? extends Table> map2;

        public CombinedMap(Map<String, ? extends Table> map1, Map<String, ? extends Table> map2)
        {
            this.map1 = map1;
            this.map2 = map2;
        }

        @Override
		public Table get(Object name) {
            Table table = map1.get(name);
            if (table == null)
                table = map2.get(name);
            return table;
        }

        @Override
		public int size() {
            return map1.size() + map2.size();
        }

        @Override
		public boolean isEmpty() {
            return map1.isEmpty() && map2.isEmpty();
        }

        @Override
		public boolean containsKey(Object key) {
            return map1.containsKey(key) || map2.containsKey(key);
        }

        @Override
		public boolean containsValue(Object value) {
            return map1.containsValue(value) || map2.containsValue(value);
        }

        @Override
		public Table put(String name, Table table) {
            throw new UnsupportedOperationException();
        }

        /**
         * Warning: potentially expensive operation
         */
        @Override
		public Set<String> keySet() {
            return getCombined().keySet();
        }

        /**
         * Warning: potentially expensive operation
         */
        @Override
		public Set<Map.Entry<String, Table>> entrySet() {
            return getCombined().entrySet();
        }

        /**
         * Warning: potentially expensive operation
         */
        @Override
		public Collection<Table> values() {
            return getCombined().values();
        }

        private Map<String, Table> getCombined() {
            Map<String, Table> all = new CaseInsensitiveMap<Table>(size());
            all.putAll(map1);
            all.putAll(map2);
            return all;
        }

        @Override
		public Table remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
		public void putAll(Map<? extends String, ? extends Table> table) {
            throw new UnsupportedOperationException();
        }

        @Override
		public void clear() {
            throw new UnsupportedOperationException();
        }
    }
}
