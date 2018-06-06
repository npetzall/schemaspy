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
package org.schemaspy;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.EmptySchemaException;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.service.SqlService;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SchemaAnalyzerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void emptySchemaSingleSchemaThrowsEmptySchemaException() throws SQLException, IOException {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.supportsSchemasInTableDefinitions()).thenReturn(true);
        when(databaseMetaData.supportsCatalogsInTableDefinitions()).thenReturn(true);

        SqlService sqlService = mock(SqlService.class);
        when(sqlService.connect(any())).thenReturn(databaseMetaData);

        DatabaseService databaseService = mock(DatabaseService.class);

        CommandLineArguments commandLineArguments = mock(CommandLineArguments.class);
        when(commandLineArguments.getCatalog()).thenReturn("catalog");

        Config config = new Config("-db", "database", "-nohtml", "-u", "user");

        ProgressListener progressListener = mock(ProgressListener.class);

        SchemaAnalyzer schemaAnalyzer = new SchemaAnalyzer(sqlService,databaseService, commandLineArguments);

        assertThatThrownBy(
                () -> schemaAnalyzer.analyze("schema", config, temporaryFolder.newFolder(), progressListener)
        ).isInstanceOf(EmptySchemaException.class);
    }

    @Test
    public void emptySchemaMultiSchemaThrowsNoEmptySchemaException() throws SQLException, IOException {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.supportsSchemasInTableDefinitions()).thenReturn(true);
        when(databaseMetaData.supportsCatalogsInTableDefinitions()).thenReturn(true);

        SqlService sqlService = mock(SqlService.class);
        when(sqlService.connect(any())).thenReturn(databaseMetaData);

        DatabaseService databaseService = mock(DatabaseService.class);

        CommandLineArguments commandLineArguments = mock(CommandLineArguments.class);
        when(commandLineArguments.getCatalog()).thenReturn("catalog");

        Config config = new Config("-db", "database", "-nohtml", "-u", "user");
        config.setOneOfMultipleSchemas(true);

        ProgressListener progressListener = mock(ProgressListener.class);

        SchemaAnalyzer schemaAnalyzer = new SchemaAnalyzer(sqlService,databaseService, commandLineArguments);

        schemaAnalyzer.analyze("schema", config, temporaryFolder.newFolder(), progressListener);
    }
}
