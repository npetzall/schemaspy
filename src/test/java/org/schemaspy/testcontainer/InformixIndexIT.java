package org.schemaspy.testcontainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.model.TableIndex;
import org.schemaspy.testing.AssumeClassIsPresentRule;
import org.schemaspy.testing.ContextRule;
import org.schemaspy.testing.DatabaseCreator;
import org.testcontainers.containers.InformixContainer;

import java.io.IOException;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

public class InformixIndexIT {

    public static TestRule jdbcDriverClassPresentRule = new AssumeClassIsPresentRule("com.informix.jdbc.IfxDriver");

    public static JdbcContainerRule<InformixContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new InformixContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/informixIndexXMLIT/dbScripts/informix.sql");

    public static ContextRule contextRule = new ContextRule(() -> new String[]{
        "-t", "informix",
                "-db", "test",
                "-s", "informix",
                "-cat", "test",
                "-server", "dev",
                "-o", "target/integrationtesting/informix",
                "-u", jdbcContainerRule.getContainer().getUsername(),
                "-p", jdbcContainerRule.getContainer().getPassword(),
                "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                "-port", jdbcContainerRule.getContainer().getJdbcPort().toString()
    });

    @ClassRule
    public static final TestRule chain = RuleChain
            .outerRule(jdbcContainerRule)
            .around(contextRule)
            .around(jdbcDriverClassPresentRule);

    private static Database database;

    @Before
    public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException {
        if (database == null) {
            database = DatabaseCreator.create(contextRule.getContext());
        }
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
