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

import org.junit.Test;
import org.schemaspy.input.dbms.xml.SchemaMeta;
import org.schemaspy.model.Database;
import org.schemaspy.model.Routine;

import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UpdateServiceIT {

    @Test
    public void willUpdateRoutineComment() throws SQLException {
        UpdateService updateService = new UpdateService(null);
        SchemaMeta schemaMeta = new SchemaMeta(Paths.get("src","test","resources","integrationTesting","updateservice","updateRoutineComment.xml").toString(),"dbName","schemaName");
        Routine routine = new Routine("add", "function", "int", "sql", "a + b", true, "user", "caller","bad comment");
        Database database = mock(Database.class);
        when(database.getRoutinesMap()).thenReturn(Collections.singletonMap("add", routine));
        assertThat(routine.getComment()).isEqualToIgnoringCase("bad comment");
        updateService.updateFromXmlMetadata(database, schemaMeta);
        assertThat(routine.getComment()).isEqualToIgnoringCase("betterComment");
    }
}