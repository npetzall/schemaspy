package org.schemaspy.service;

import org.schemaspy.api.progress.ProgressListener;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.service.helper.BasicTableMeta;

import java.util.concurrent.Callable;

public class CallableTableCreator implements Callable<Table>{

    private final Database database;
    private final BasicTableMeta basicTableMeta;
    private final TableService tableService;
    private final boolean withRowCount;

    public CallableTableCreator(Database database, BasicTableMeta basicTableMeta, TableService tableService, boolean withRowCount, ProgressListener tableCreatorProgressListener) {
        this.database = database;
        this.basicTableMeta = basicTableMeta;
        this.tableService = tableService;
        this.withRowCount = withRowCount;
    }

    @Override
    public Table call() throws Exception {
        Table table = new Table(
                database.getName(),
                basicTableMeta.getCatalog(),
                basicTableMeta.getSchema(),
                basicTableMeta.getName(),
                basicTableMeta.getRemarks());

        tableService.gatheringTableDetails(database, table);

        if (basicTableMeta.getNumRows() != -1) {
            table.setNumRows(basicTableMeta.getNumRows());
        }

        if (table.getNumRows() == 0) {
            long numRows = withRowCount ? tableService.fetchNumRows(database, table) : -1;
            table.setNumRows(numRows);
        }

        return table;
    }
}
