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

import org.junit.BeforeClass;
import org.junit.Test;
import org.schemaspy.model.Routine;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HtmlRoutinePageTest {

    private static HtmlRoutinePage htmlRoutinePage;
    private static Routine routineWithDefinitionNoComment = new Routine("hasDefinition", "FUNCTION", "char(50", "SQL", "SELECT * FROM SOMEWHERE WHERE 1=1", true, "IN", "n/A", null);
    private static Routine routineWithoutDefinitionWithComment = new Routine("hasNoDefinition", "FUNCTION", "char(50", "SQL", null, true, "IN", "n/A", "yepp, do I have a comment");

    @BeforeClass
    public static void setup() {
        HtmlConfig singleConfig = mock(HtmlConfig.class);
        when(singleConfig.getTemplateDirectory()).thenReturn("layout");
        when(singleConfig.isOneOfMultipleSchemas()).thenReturn(false);
        MustacheCompiler mustacheCompiler = new MustacheCompiler("testing", singleConfig);
        htmlRoutinePage = new HtmlRoutinePage(mustacheCompiler);
    }

    @Test
    public void definitionExists() {
        StringWriter stringWriter = new StringWriter();
        htmlRoutinePage.write(routineWithDefinitionNoComment, stringWriter);
        assertThat(stringWriter.toString()).contains("id=\"RoutineDefinition\"");
        assertThat(stringWriter.toString()).contains(routineWithDefinitionNoComment.getDefinition());
    }

    @Test
    public void definitionDoesNotExist() {
        StringWriter stringWriter = new StringWriter();
        htmlRoutinePage.write(routineWithoutDefinitionWithComment, stringWriter);
        assertThat(stringWriter.toString()).doesNotContain("id=\"RoutineDefinition\"");
    }

    @Test
    public void commentExists() {
        StringWriter stringWriter = new StringWriter();
        htmlRoutinePage.write(routineWithoutDefinitionWithComment, stringWriter);
        assertThat(stringWriter.toString()).contains("id=\"Description\"");
        assertThat(stringWriter.toString()).contains(routineWithoutDefinitionWithComment.getComment());
    }

    @Test
    public void commentDoesNotExist() {
        StringWriter stringWriter = new StringWriter();
        htmlRoutinePage.write(routineWithDefinitionNoComment, stringWriter);
        assertThat(stringWriter.toString()).doesNotContain("id=\"Description\"");
    }

}