package org.schemaspy.input.dbms.dbmsinputsource;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.schemaspy.input.dbms.DbmsInputSource;
import org.schemaspy.input.dbms.testing.DbmsInputConfigurationFluent;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.testing.AssumeClassIsPresentRule;
import org.testcontainers.containers.OracleContainer;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class OracleIT {


    public static TestRule jdbcDriverClassPresentRule = new AssumeClassIsPresentRule("oracle.jdbc.OracleDriver");

    public static JdbcContainerRule<OracleContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new OracleContainer())
            .assumeDockerIsPresent()
            .withAssumptions(assumeDriverIsPresent())
            .withInitScript("integrationTesting/dbScripts/oracle.sql");

    @ClassRule
    public static final TestRule chain = RuleChain
            .outerRule(jdbcContainerRule)
            .around(jdbcDriverClassPresentRule);

    private static Database database;

    @Before
    public synchronized void gatheringSchemaDetailsTest() {
        if (database == null) {
            createDatabaseRepresentation();
        }
    }

    private void createDatabaseRepresentation() {
        DbmsInputConfigurationFluent dbmsInputConfigurationFluent = new DbmsInputConfigurationFluent()
                .setDatabaseType("orathin")
                .setDatabaseName(jdbcContainerRule.getContainer().getSid())
                .setSchema("ORAIT")
                .setCatalog("%")
                .setHost(jdbcContainerRule.getContainer().getContainerIpAddress())
                .setPort(jdbcContainerRule.getContainer().getOraclePort().toString())
                .setUser("orait")
                .setPassword("orait123");

        ProgressListener progressListener = mock(ProgressListener.class);
        DbmsInputSource dbmsInputSource = new DbmsInputSource(dbmsInputConfigurationFluent, progressListener);
        this.database = dbmsInputSource.createDatabaseModel();
    }

    @Test
    public void databaseShouldBePopulatedWithTableTest() {
        Table table = getTable("TEST");
        assertThat(table).isNotNull();
    }

    @Test
    public void databaseShouldBePopulatedWithTableTestAndHaveColumnName() {
        Table table = getTable("TEST");
        TableColumn column = table.getColumn("NAME");
        assertThat(column).isNotNull();
    }

    @Test
    public void databaseShouldBePopulatedWithTableTestAndHaveColumnNameWithComment() {
        Table table = getTable("TEST");
        TableColumn column = table.getColumn("NAME");
        assertThat(column.getComments()).isEqualToIgnoringCase("the name");
    }

    private Table getTable(String tableName) {
        return database.getTablesByName().get(tableName);
    }
}
