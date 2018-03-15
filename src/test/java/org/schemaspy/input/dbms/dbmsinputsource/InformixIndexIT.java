package org.schemaspy.input.dbms.dbmsinputsource;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.schemaspy.input.dbms.DbmsInputSource;
import org.schemaspy.input.dbms.testing.DbmsInputConfigurationFluent;
import org.schemaspy.model.*;
import org.schemaspy.testing.AssumeClassIsPresentRule;
import org.testcontainers.containers.InformixContainer;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class InformixIndexIT {

    public static TestRule jdbcDriverClassPresentRule = new AssumeClassIsPresentRule("com.informix.jdbc.IfxDriver");

    public static JdbcContainerRule<InformixContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new InformixContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/informixIndexXMLIT/dbScripts/informix.sql");

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
                .setDatabaseType("informix")
                .setDatabaseName("test")
                .setSchema("informix")
                .setCatalog("test")
                .setHost(jdbcContainerRule.getContainer().getContainerIpAddress())
                .setPort(jdbcContainerRule.getContainer().getJdbcPort().toString())
                .setUser(jdbcContainerRule.getContainer().getUsername())
                .setPassword(jdbcContainerRule.getContainer().getPassword())
                .addArgument("server", "dev");

        ProgressListener progressListener = mock(ProgressListener.class);
        DbmsInputSource dbmsInputSource = new DbmsInputSource(dbmsInputConfigurationFluent, progressListener);
        this.database = dbmsInputSource.createDatabaseModel();
    }

    @Test
    public void databaseShouldBePopulatedWithTableTest() {
        Table table = getTable("test");
        assertThat(table).isNotNull();
    }

    @Test
    public void databaseShouldBePopulatedWithTableTestAndHaveColumnName() {
        Table table = getTable("test");
        TableColumn column = table.getColumn("firstname");
        assertThat(column).isNotNull();
    }

    @Test
    public void tableTestShouldHaveTwoIndexes() {
        Table table = getTable("test");
        assertThat(table.getIndexes().size()).isEqualTo(2);
    }

    @Test
    public void tableTestIndex_test_index_shouldHaveThreeColumns() {
        TableIndex index = getTable("test").getIndex("test_index");
        assertThat(index.getColumns().size()).isEqualTo(3);
    }

    private Table getTable(String tableName) {
        return database.getTablesByName().get(tableName);
    }
}
