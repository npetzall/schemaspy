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
import org.schemaspy.testing.AssumeClassIsPresentRule;
import org.schemaspy.testing.ContextRule;
import org.schemaspy.testing.DatabaseCreator;
import org.testcontainers.containers.OracleContainer;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

public class OracleIT {


    public static TestRule jdbcDriverClassPresentRule = new AssumeClassIsPresentRule("oracle.jdbc.OracleDriver");

    public static JdbcContainerRule<OracleContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new OracleContainer())
            .assumeDockerIsPresent()
            .withAssumptions(assumeDriverIsPresent())
            .withInitScript("integrationTesting/dbScripts/oracle.sql");

    public static ContextRule contextRule = new ContextRule(() -> new String[]{
            "-t", "orathin",
            "-db", jdbcContainerRule.getContainer().getSid(),
            "-s", "ORAIT",
            "-cat", "%",
            "-o", "target/integrationtesting/orait",
            "-u", "orait",
            "-p", "orait123",
            "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
            "-port", jdbcContainerRule.getContainer().getOraclePort().toString()
    });

    @ClassRule
    public static final TestRule chain = RuleChain
            .outerRule(jdbcContainerRule)
            .around(contextRule)
            .around(jdbcDriverClassPresentRule);

    private static Database database;

    @Before
    public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            database = DatabaseCreator.create(contextRule.getContext());
        }
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
