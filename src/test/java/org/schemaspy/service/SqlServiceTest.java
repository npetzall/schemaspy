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
package org.schemaspy.service;

import org.junit.Test;
import org.schemaspy.Config;
import org.schemaspy.DbDriverLoader;
import org.schemaspy.model.DbmsMeta;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SqlServiceTest {

    @Test
    public void canOverrideIdentifierQuoteString() throws IOException, SQLException {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getExtraNameCharacters()).thenReturn("");
        Connection connection = mock(Connection.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        DbDriverLoader driverLoader = mock(DbDriverLoader.class);
        when(driverLoader.getConnection(any())).thenReturn(connection);
        DbmsMeta dbmsMeta = new DbmsMeta.Builder().identifierQuoteString("&").getDbmsMeta();
        DbmsService dbmsService = mock(DbmsService.class);
        when(dbmsService.fetchDbmsMeta(any())).thenReturn(dbmsMeta);
        SqlService sqlService = new SqlService(driverLoader,dbmsService);
        sqlService.connect(new Config());
        assertThat(sqlService.quoteIdentifier("table")).isEqualTo("&table&");
        sqlService.connect(new Config("-t","QuoteStringOverride"));
        assertThat(sqlService.quoteIdentifier("table")).isEqualTo("%table%");
    }
}
