package org.schemaspy.input.dbms.dbmsinputsource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.schemaspy.input.dbms.DbmsInputSource;
import org.schemaspy.input.dbms.testing.DbmsInputConfigurationFluent;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.testing.H2MemoryRule;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class H2KeywordIT {

    @ClassRule
    public static H2MemoryRule h2MemoryRule = new H2MemoryRule("h2keyword").addSqlScript("src/test/resources/integrationTesting/h2KeywordIT/dbScripts/keyword_in_table.sql");

    private static Database database;

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException {
        DbmsInputConfigurationFluent dbmsInputConfigurationFluent = new DbmsInputConfigurationFluent()
                .setDatabaseType("src/test/resources/integrationTesting/dbTypes/h2memory")
                .setDatabaseName("h2keyword")
                .setSchema(h2MemoryRule.getConnection().getSchema())
                .setCatalog(h2MemoryRule.getConnection().getCatalog())
                .setUser("sa");

        ProgressListener progressListener = mock(ProgressListener.class);
        DbmsInputSource dbmsInputSource = new DbmsInputSource(dbmsInputConfigurationFluent, progressListener);
        this.database = dbmsInputSource.createDatabaseModel();
    }

    @Test
    public void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("h2keyword");
    }

    @Test
    public void tableWithKeyWordShouldExist() {
        assertThat(database.getTables()).extracting(t -> t.getName()).contains("DISTINCT");
    }
}
