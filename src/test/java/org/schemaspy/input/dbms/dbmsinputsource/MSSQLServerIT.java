package org.schemaspy.input.dbms.dbmsinputsource;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.schemaspy.input.dbms.DbmsInputSource;
import org.schemaspy.input.dbms.testing.DbmsInputConfigurationFluent;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.testcontainers.containers.MSSQLServerContainer;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MSSQLServerIT {

    @ClassRule
    public static JdbcContainerRule<MSSQLServerContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MSSQLServerContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/dbScripts/mssql.sql");

    private static Database database;

    @Before
    public synchronized void gatheringSchemaDetailsTest() {
        if (database == null) {
            createDatabaseRepresentation();
        }
    }

    private void createDatabaseRepresentation() {
        DbmsInputConfigurationFluent dbmsInputConfigurationFluent = new DbmsInputConfigurationFluent()
                .setDatabaseType("mssql08")
                .setDatabaseName("test")
                .setSchema("dbo")
                .setCatalog("%")
                .setHost(jdbcContainerRule.getContainer().getContainerIpAddress())
                .setPort(jdbcContainerRule.getContainer().getMappedPort(1433).toString())
                .setUser(jdbcContainerRule.getContainer().getUsername())
                .setPassword(jdbcContainerRule.getContainer().getPassword());

        ProgressListener progressListener = mock(ProgressListener.class);
        DbmsInputSource dbmsInputSource = new DbmsInputSource(dbmsInputConfigurationFluent, progressListener);
        this.database = dbmsInputSource.createDatabaseModel();
    }

    @Test
    public void databaseShouldBePopulatedWithTableTest() {
        Table table = getTable("TestTable");
        assertThat(table).isNotNull();
    }

    @Test
    public void databaseShouldBePopulatedWithTableTestAndHaveColumnName() {
        Table table = getTable("TestTable");
        TableColumn column = table.getColumn("Description");
        assertThat(column).isNotNull();
    }

    @Test
    public void databaseShouldBePopulatedWithTableTestAndHaveColumnNameWithComment() {
        Table table = getTable("TestTable");
        TableColumn column = table.getColumn("Description");
        assertThat(column.getComments()).isEqualToIgnoringCase("This is column description");
    }

    private Table getTable(String tableName) {
        return database.getTablesByName().get(tableName);
    }
}
