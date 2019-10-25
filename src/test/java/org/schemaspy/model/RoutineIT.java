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
package org.schemaspy.model;

import org.junit.Test;
import org.schemaspy.input.dbms.xml.RoutineMeta;

import static org.assertj.core.api.Assertions.assertThat;

public class RoutineIT {

    @Test
    public void routineCommentCanBeUpdatedByRoutineMeta() {
        Routine routine = new Routine("r","sql", "string", "sql", "def", true, "caller", "caller", "firstComment");
        RoutineMeta routineMeta = new RoutineMeta("r", "secondComment");
        routine.update(routineMeta);
        assertThat(routine.getComment()).isEqualToIgnoringCase("secondComment");
    }
}