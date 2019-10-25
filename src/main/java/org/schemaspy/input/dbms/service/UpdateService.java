/*
 * Copyright (C) 2019 Nils Petzaell
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
package org.schemaspy.input.dbms.service;

import org.schemaspy.input.dbms.service.helper.RemoteTableIdentifier;
import org.schemaspy.input.dbms.xml.RoutineMeta;
import org.schemaspy.input.dbms.xml.SchemaMeta;
import org.schemaspy.input.dbms.xml.TableMeta;
import org.schemaspy.model.Database;
import org.schemaspy.model.LogicalTable;
import org.schemaspy.model.Routine;
import org.schemaspy.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.Objects;

public class UpdateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TableService tableService;

    public UpdateService(TableService tableService) {
        this.tableService = tableService;
    }

    /**
     * Take the supplied XML-based metadata and update our model of the schema with it
     *
     * @param schemaMeta
     * @throws SQLException
     */
    public void updateFromXmlMetadata(Database db, SchemaMeta schemaMeta) throws SQLException {
        if (Objects.isNull(schemaMeta)) {
            return;
        }
        if (Objects.nonNull(schemaMeta.getComments())) {
            db.getSchema().setComment(schemaMeta.getComments());
        }

        // done in three passes:
        // 1: create any new tables
        // 2: add/mod columns
        // 3: connect

        // add the newly defined tables and columns first
        for (TableMeta tableMeta : schemaMeta.getTables()) {
            Table table;

            if (tableMeta.getRemoteSchema() != null || tableMeta.getRemoteCatalog() != null) {
                // will add it if it doesn't already exist
                table = tableService.addLogicalRemoteTable(db, RemoteTableIdentifier.from(tableMeta), db.getSchema().getName());
            } else {
                table = db.getLocals().get(tableMeta.getName());

                if (table == null) {
                    // new table defined only in XML metadata
                    table = new LogicalTable(db, db.getCatalog().getName(), db.getSchema().getName(), tableMeta.getName(), tableMeta.getComments());
                    db.getTablesMap().put(table.getName(), table);
                }
            }

            table.update(tableMeta);
        }

        // then tie the tables together
        for (TableMeta tableMeta : schemaMeta.getTables()) {
            Table table;

            if (tableMeta.getRemoteCatalog() != null || tableMeta.getRemoteSchema() != null) {
                table = db.getRemoteTablesMap().get(db.getRemoteTableKey(tableMeta.getRemoteCatalog(), tableMeta.getRemoteSchema(), tableMeta.getName()));
            } else {
                table = db.getLocals().get(tableMeta.getName());
            }

            tableService.connect(db, table, tableMeta, db.getLocals());
        }

        for (RoutineMeta routineMeta : schemaMeta.getRoutines()) {
            Routine routine = db.getRoutinesMap().get(routineMeta.name);
            if (Objects.nonNull(routine)) {
                routine.update(routineMeta);
            } else {
                LOGGER.info("Found metadata for routine {}, but the routine is missing", routineMeta.name);
            }
        }
    }
}
