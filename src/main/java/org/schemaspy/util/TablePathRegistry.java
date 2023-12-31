package org.schemaspy.util;

import org.schemaspy.model.Table;
import org.schemaspy.util.naming.FileNameGenerator;

import java.util.Collection;
import java.util.HashMap;

public class TablePathRegistry {

    private final HashMap<String,String> paths = new HashMap<>();

    public TablePathRegistry addTables(final Collection<? extends Table> tables) {
        tables.forEach(this::addTable);
        return this;
    }

    private void addTable(final Table table) {
        if (!table.isLogical()) {
            paths.put(table.getName(), "tables/" + new FileNameGenerator(table.getName()).value() + ".html");
        }
    }

    public String pathForTableName(final String tableName) {
        return paths.get(tableName);
    }

}
