package org.schemaspy.input.dbms.dbmsinputsource;

import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.input.dbms.DbmsInputSource;
import org.schemaspy.input.dbms.testing.DbmsInputConfigurationFluent;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.testing.H2MemoryRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DatabaseServiceIT {

    private static String CREATE_SCHEMA = "CREATE SCHEMA DATABASESERVICEIT AUTHORIZATION SA";
    private static String SET_SCHEMA = "SET SCHEMA DATABASESERVICEIT";
    private static String CREATE_TABLE = "CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))";

    @Rule
    public H2MemoryRule h2MemoryRule = new H2MemoryRule("DatabaseServiceIT").addSqls(CREATE_SCHEMA, SET_SCHEMA, CREATE_TABLE);

    @Test
    public void gatheringSchemaDetailsTest() throws Exception {
        DbmsInputConfigurationFluent dbmsInputConfigurationFluent = new DbmsInputConfigurationFluent()
                .setDatabaseType("src/test/resources/integrationTesting/dbTypes/h2memory")
                .setDatabaseName("DatabaseServiceIT")
                .setSchema(h2MemoryRule.getConnection().getSchema())
                .setCatalog(h2MemoryRule.getConnection().getCatalog())
                .setUser("sa");

        ProgressListener progressListener = mock(ProgressListener.class);
        DbmsInputSource dbmsInputSource = new DbmsInputSource(dbmsInputConfigurationFluent, progressListener);
        Database database = dbmsInputSource.createDatabaseModel();

        assertThat(database.getTables()).hasSize(1);
        assertThat(database.getTables().stream().findFirst().get().getName()).isEqualToIgnoringCase("TEST");
    }
}
