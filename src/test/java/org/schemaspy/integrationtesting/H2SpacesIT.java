package org.schemaspy.integrationtesting;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.schemaspy.model.Database;
import org.schemaspy.testing.ContextRule;
import org.schemaspy.testing.DatabaseCreator;
import org.schemaspy.testing.H2MemoryRule;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class H2SpacesIT {

    @ClassRule
    public static H2MemoryRule h2MemoryRule = new H2MemoryRule("h2 spaces").addSqlScript("src/test/resources/integrationTesting/h2SpacesIT/dbScripts/spaces_in_schema_and_table.sql");

    private static String[] args = {
            "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
            "-db", "h2 spaces",
            "-cat", "PUBLIC",
            "-s", "h2 spaces",
            "-o", "target/integrationtesting/h2 spaces",
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
        assertThat(database.getName()).isEqualToIgnoringCase("h2 spaces");
    }

    @Test
    public void tableWithSpacesShouldExist() {
        assertThat(database.getTables()).extracting(t -> t.getName()).contains("has space");
    }
}
