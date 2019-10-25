/*
 * Copyright (C) 2017 Nils Petzaell
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
package org.schemaspy.input.dbms.xml;

import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.testing.Logger;
import org.schemaspy.testing.LoggingRule;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
public class SchemaMetaTest {

    @Rule
    public LoggingRule LOGGING = new LoggingRule();

    @Test
    @Logger(TableMeta.class)
    public void deprecationWarningRemarksInTable() {
        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/model/xml/remarksInTable.xml", "dbname", "schemaName");
        assertThat(LOGGING.getLog()).contains("deprecated");
    }

    @Test
    @Logger(TableColumnMeta.class)
    public void deprecationWarningRemarksInColumn() {
        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/model/xml/remarksInColumn.xml", "dbname", "schemaName");
        assertThat(LOGGING.getLog()).contains("deprecated");
    }

    @Test
    @Logger(TableMeta.class)
    public void okCommentsInTable() {
        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/model/xml/commentsInTable.xml", "dbname", "schemaName");
        assertThat(LOGGING.getLog()).doesNotContain("deprecated");
    }

    @Test
    @Logger(TableColumnMeta.class)
    public void okCommentsInColumn() {
        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/model/xml/commentsInColumn.xml", "dbname", "schemaName");
        assertThat(LOGGING.getLog()).doesNotContain("deprecated");
    }

    @Test
    public void canParseRoutineComments() {
        SchemaMeta schemaMeta = new SchemaMeta(Paths.get("src","test","resources","unit","schemameta","with_routines.xml").toString(), "dbName", "schemaName");
        assertThat(schemaMeta.getRoutines()).usingElementComparator((rm1, rm2) -> rm1.name.compareTo(rm2.name) + rm1.comments.compareTo(rm2.comments)).containsExactly(new RoutineMeta("r_a","r_c"), new RoutineMeta("r_aa", "r_cc"));
    }

}
