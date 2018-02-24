package org.schemaspy.service;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.testing.ContextRule;
import org.schemaspy.testing.DatabaseCreator;
import org.schemaspy.testing.H2MemoryRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DatabaseServiceIT {

    private static String CREATE_SCHEMA = "CREATE SCHEMA DATABASESERVICEIT AUTHORIZATION SA";
    private static String SET_SCHEMA = "SET SCHEMA DATABASESERVICEIT";
    private static String CREATE_TABLE = "CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))";

    @Rule
    public H2MemoryRule h2MemoryRule = new H2MemoryRule("DatabaseServiceIT").addSqls(CREATE_SCHEMA, SET_SCHEMA, CREATE_TABLE);

    private static String[] args = {
            "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
            "-db", "DatabaseServiceIT",
            "-s", "DATABASESERVICEIT",
            "-cat", "DATABASESERVICEIT",
            "-o", "target/integrationtesting/databaseServiceIT",
            "-u", "sa"
    };

    @ClassRule
    public static ContextRule contextRule = new ContextRule(args);

    private static final ProgressListener progressListener = mock(ProgressListener.class);

    @Test
    public void gatheringSchemaDetailsTest() throws Exception {
        Database database = DatabaseCreator.create(contextRule.getContext());
        assertThat(database.getTables()).hasSize(1);
    }
}
