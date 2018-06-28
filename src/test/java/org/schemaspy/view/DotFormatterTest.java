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
package org.schemaspy.view;

import org.junit.Test;
import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.testing.TestingLineWriter;
import org.schemaspy.util.LineWriter;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DotFormatterTest {

    @Test
    public void dotFileWithConnectionsBetweenParentAndChild() throws IOException {
        DotFormatter formatter = DotFormatter.getInstance();
        File outputDir = mock(File.class);
        when(outputDir.toString()).thenReturn("output");
        Database database = mock(Database.class);
        Table invoice = new Table(database, "catalog", "schema", "invoice", "the main table, fk in users");
        TableColumn invoiceId = new TableColumn(invoice);
        invoiceId.setName("id");
        invoiceId.setType(4);
        invoiceId.setShortType("INT");
        invoice.getColumnsMap().put("id", invoiceId);
        invoice.setPrimaryColumn(invoiceId);
        TableColumn invoiceUserId = new TableColumn(invoice);
        invoiceUserId.setName("userId");
        invoiceUserId.setType(4);
        invoiceUserId.setShortType("INT");
        invoice.getColumnsMap().put("userId", invoiceUserId);

        Table invoiceLines = new Table(database, "catalog", "schema", "invoiceLines", "fk in invoice, fk in users");
        TableColumn invoiceLinesId = new TableColumn(invoiceLines);
        invoiceLinesId.setName("id");
        invoiceLinesId.setType(4);
        invoiceLinesId.setShortType("INT");
        invoiceLines.getColumnsMap().put("id", invoiceLinesId);
        invoiceLines.setPrimaryColumn(invoiceLinesId);
        TableColumn invoiceLinesInvoiceId = new TableColumn(invoiceLines);
        invoiceLinesInvoiceId.setName("invoiceId");
        invoiceLinesInvoiceId.setType(4);
        invoiceLinesInvoiceId.setShortType("INT");
        invoiceLines.getColumnsMap().put("invoiceId", invoiceLinesInvoiceId);
        TableColumn invoiceLinesUserId = new TableColumn(invoiceLines);
        invoiceLinesUserId.setName("userId");
        invoiceLinesUserId.setType(4);
        invoiceLinesUserId.setShortType("INT");
        invoiceLines.getColumnsMap().put("userId", invoiceLinesUserId);

        Table users = new Table(database, "catalog", "schema", "users", "users");
        TableColumn usersId = new TableColumn(users);
        usersId.setName("id");
        usersId.setType(4);
        usersId.setShortType("INT");
        usersId.setAllExcluded(false);
        usersId.setExcluded(true);
        users.getColumnsMap().put("id", usersId);
        users.setPrimaryColumn(usersId);

        invoice.getForeignKeysMap().put("FK_user", new ForeignKeyConstraint(usersId, invoiceUserId));
        invoiceLines.getForeignKeysMap().put("FK_invoce", new ForeignKeyConstraint(invoiceId, invoiceLinesInvoiceId));
        invoiceLines.getForeignKeysMap().put("FK_users", new ForeignKeyConstraint(usersId, invoiceLinesUserId));


        LineWriter dotOut = new TestingLineWriter();
        WriteStats oneStats = new WriteStats(Collections.emptyList());
        formatter.writeRealRelationships(invoice, false, oneStats, dotOut, outputDir);

        //Should have
        assertThat(dotOut.toString()).contains("\"invoiceLines\":\"userId\":w -> \"users\":\"id\":e");
        //Should not have
        //assertThat(dotOut.toString()).doesNotContain("\"invoiceLines\":\"userId\":w -> \"users\":\"id\":e");
    }

}