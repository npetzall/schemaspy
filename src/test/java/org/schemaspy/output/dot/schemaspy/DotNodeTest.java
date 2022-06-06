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
package org.schemaspy.output.dot.schemaspy;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schemaspy.SimpleDotConfig;
import org.schemaspy.model.*;
import org.schemaspy.output.dot.schemaspy.link.RelativeTableNodeLinkFactory;
import org.schemaspy.output.dot.schemaspy.link.TableNodeLinkFactory;
import org.schemaspy.output.dot.schemaspy.link.WithTargetTopTableNodeLinkFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Nils Petzaell
 */
class DotNodeTest {

    private static final Path expectations = Paths.get("src", "test", "resources", "dotnode");

    private static List<String> getLines(String fileName) throws IOException {
        return Files.readAllLines(expectations.resolve(fileName), StandardCharsets.UTF_8);
    }

    private static final FontConfig fontConfig = new TestFontConfig();

    private static final Database database = mock(Database.class);
    private static final Table localTable = new LogicalTable(database, "catalog", "schema", "a table", "comment");
    private static final Table remoteTable = new RemoteTable(database, "catalog", "schema", "a table", "remote");

    private final TableNodeLinkFactory tableNodeLinkFactory = new WithTargetTopTableNodeLinkFactory(new RelativeTableNodeLinkFactory());

    @BeforeAll
    static void setup() {
        TableColumn localPK = createLocalColumn("primaryKey");
        localPK.setId(1);
        localTable.getPrimaryColumns().add(localPK);
        TableColumn localFK = createLocalColumn("foreignKey");
        localFK.setId(2);
        localTable.getForeignKeysMap().put("simple_Fk", new ForeignKeyConstraint(createRemoteColumn("remotePK"), localFK));
        new ForeignKeyConstraint(localPK, createRemoteColumn("remoteFK"));
        TableIndex tableIndex = new TableIndex("index_qx", true);
        TableColumn indexed = createLocalColumn("indexed");
        indexed.setId(3);
        tableIndex.addColumn(indexed, "desc");
        localTable.getIndexesMap().put("index_qx", tableIndex);
        TableColumn implied = createLocalColumn("simple_column");
        implied.setId(4);
        new ImpliedForeignKeyConstraint(createRemoteColumn("impliedKey"), implied);
        new ImpliedForeignKeyConstraint(indexed, createRemoteColumn("remoteImpliedFK"));
        localTable.setNumRows(100000L);
        createLocalColumn("trivialColumn").setId(5);
    }

    private static TableColumn createLocalColumn(String name) {
        return createColumn(localTable, name);
    }

    private static TableColumn createRemoteColumn(String name) {
        return createColumn(remoteTable, name);
    }

    private static TableColumn createColumn(Table table, String name) {
        TableColumn tableColumn = new TableColumn(table);
        tableColumn.setName(name);
        tableColumn.setShortType("int");
        tableColumn.setDetailedSize("16");
        table.getColumnsMap().put(name, tableColumn);
        return tableColumn;
    }

    @Test
    void trivialAndDetails() throws IOException {
        assertDotNode(new DotNode(
                localTable,
                tableNodeLinkFactory,
                new DotNodeConfig(
                        true,
                        true),
                new SimpleDotConfig(
                        fontConfig,
                        false,
                        false
                )
        ), "trivialAndDetails.txt");
    }

    @Test
    void trivialNoDetails() throws IOException {
        assertDotNode(new DotNode(
                localTable,
                tableNodeLinkFactory,
                new DotNodeConfig(
                        true,
                        false),
                new SimpleDotConfig(
                        fontConfig,
                        false,
                        false
                )
        ), "trivialNoDetails.txt");
    }

    @Test
    void noTrivialNoDetails() throws IOException {
        assertDotNode(new DotNode(
                localTable,
                tableNodeLinkFactory,
                new DotNodeConfig(
                        false,
                        false),
                new SimpleDotConfig(
                        fontConfig,
                        false,
                        false
                )
        ), "noTrivialNoDetails.txt");
    }

    @Test
    void noTrivialAndDetails() throws IOException {
        assertDotNode(new DotNode(
                localTable,
                tableNodeLinkFactory,
                new DotNodeConfig(
                        false,
                        true),
                new SimpleDotConfig(
                        fontConfig,
                        false,
                        false
                )
        ), "noTrivialAndDetails.txt");
    }

    @Test
    void trivialAndDetailsWithShowImplied() throws IOException {
        DotNode dotNode = new DotNode(
                localTable,
                tableNodeLinkFactory,
                new DotNodeConfig(
                        true,
                        true),
                new SimpleDotConfig(
                        fontConfig,
                        false,
                        false
                )
        );
        dotNode.setShowImplied(true);
        assertDotNode(dotNode, "trivialAndDetailsWithShowImplied.txt");
    }

    @Test
    void excludedColumn() throws IOException {
        Table table = new LogicalTable(database, "catalog", "schema", "a table", "comment");
        TableColumn prim = createColumn(table, "prim");
        table.setPrimaryColumn(prim);
        TableColumn excl = createColumn(table, "excl");
        excl.setExcluded(true);
        DotNode dotNode = new DotNode(
                table,
                tableNodeLinkFactory,
                new DotNodeConfig(true, true),
                new SimpleDotConfig(
                        fontConfig,
                        true,
                        true
                )
        );
        assertDotNode(dotNode, "excludedColumn.txt");
    }

    private void assertDotNode(DotNode dotNode, String fileName) throws IOException {
        List<String> actualLines = Arrays.asList(dotNode.value().split(System.lineSeparator()));
        List<String> expectLines = getLines(fileName);
        assertThat(actualLines).hasSameSizeAs(expectLines);
        SoftAssertions.assertSoftly(soft -> {
            for (int i = 0; i < expectLines.size(); i++) {
                soft
                        .assertThat(actualLines.get(i))
                        .describedAs("Diff on line %s", i + 1)
                        .isEqualTo(expectLines.get(i));
            }
        });
    }

    @Test
    void singularRows() {
        Table table = new LogicalTable(database, "catalog", "schema", "a table", "comment");
        TableColumn prim = createColumn(table, "prim");
        table.setPrimaryColumn(prim);
        DotNode dotNode = new DotNode(
                table,
                tableNodeLinkFactory,
                new DotNodeConfig(true, true),
                new SimpleDotConfig(
                        fontConfig,
                        true,
                        true
                )
        );
        table.setNumRows(1);
        assertThat(dotNode.value()).contains("row");
    }

    @Test
    void pluralRows() {
        Table table = new LogicalTable(database, "catalog", "schema", "a table", "comment");
        TableColumn prim = createColumn(table, "prim");
        table.setPrimaryColumn(prim);
        DotNode dotNode = new DotNode(
                table,
                tableNodeLinkFactory,
                new DotNodeConfig(true, true),
                new SimpleDotConfig(
                        fontConfig,
                        true,
                        true
                )
        );
        table.setNumRows(2);
        assertThat(dotNode.value()).contains("rows");
    }

    @Test
    void skipRows() {
        Table table = new LogicalTable(database, "catalog", "schema", "a table", "comment");
        TableColumn prim = createColumn(table, "prim");
        table.setPrimaryColumn(prim);
        DotNode dotNode = new DotNode(
                table,
                tableNodeLinkFactory,
                new DotNodeConfig(true, true),
                new SimpleDotConfig(
                        fontConfig,
                        true,
                        false
                )
        );
        table.setNumRows(1);
        assertThat(dotNode.value()).doesNotContain("row");
    }

    @Test
    void escapeHtml() {
        Table table = new LogicalTable(database, "catalog", "schema", "<table>", "comment");
        DotNode dotNode = new DotNode(
                table,
                tableNodeLinkFactory,
                new DotNodeConfig(true, true),
                new SimpleDotConfig(
                        fontConfig,
                        false,
                        false
                )
        );
        assertThat(dotNode.value()).contains("tooltip=\"&lt;table&gt;");
    }

    @Test
    void htmlEscapeShortType() {
        Table table = new LogicalTable(database, "catalog", "schema", "a table", "comment");
        TableColumn tableColumn = new TableColumn(table);
        tableColumn.setName("<A>");
        tableColumn.setShortType("<T>");
        tableColumn.setDetailedSize(null);
        table.getColumnsMap().put("<A>", tableColumn);
        DotNode dotNode = new DotNode(
                table,
                tableNodeLinkFactory,
                new DotNodeConfig(true, true),
                new SimpleDotConfig(
                        fontConfig,
                        false,
                        false
                )
        );
        assertThat(dotNode.value()).contains("<TD PORT=\"&lt;A&gt;.type\" ALIGN=\"LEFT\">&lt;t&gt;</TD>");
    }

    @Test
    void htmlEscapeDetailedSize() {
        Table table = new LogicalTable(database, "catalog", "schema", "a table", "comment");
        TableColumn tableColumn = new TableColumn(table);
        tableColumn.setName("<A>");
        tableColumn.setShortType("<T>");
        tableColumn.setDetailedSize("<D>");
        table.getColumnsMap().put("<A>", tableColumn);
        DotNode dotNode = new DotNode(
                table,
                tableNodeLinkFactory,
                new DotNodeConfig(true, true),
                new SimpleDotConfig(
                        fontConfig,
                        false,
                        false
                )
        );
        assertThat(dotNode.value()).contains("[&lt;D&gt;]");
    }
}