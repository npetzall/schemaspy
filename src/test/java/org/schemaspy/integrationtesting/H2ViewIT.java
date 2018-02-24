package org.schemaspy.integrationtesting;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.schemaspy.Config;
import org.schemaspy.app.Context;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.testing.ContextRule;
import org.schemaspy.testing.DatabaseCreator;
import org.schemaspy.testing.H2MemoryRule;

import java.io.File;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class H2ViewIT {

    @ClassRule
    public static H2MemoryRule h2MemoryRule = new H2MemoryRule("h2view").addSqlScript("src/test/resources/integrationTesting/h2ViewIT/dbScripts/2tables1view.sql");

    private static String[] args = {
            "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
            "-db", "h2view",
            "-cat", "PUBLIC",
            "-s", "h2view",
            "-o", "target/integrationtesting/h2view",
            "-u", "sa"
    };

    @ClassRule
    public static ContextRule contextRule = new ContextRule(args);

    private static Database database;

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException {
        if (database == null) {
            database = DatabaseCreator.create(contextRule.getContext());
        }
    }

    @Test
    public void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("h2view");
    }

    @Test
    public void viewShouldExist() {
        assertThat(database.getViews()).extracting(v -> v.getName()).contains("THE_VIEW");
        assertThat(database.getViewsMap().get("THE_VIEW").getViewDefinition()).isNotBlank();
    }
}
