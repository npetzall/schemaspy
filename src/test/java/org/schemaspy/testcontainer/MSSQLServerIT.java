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
import org.schemaspy.testing.ContextRule;
import org.schemaspy.testing.DatabaseCreator;
import org.testcontainers.containers.MSSQLServerContainer;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

public class MSSQLServerIT {

    public static JdbcContainerRule<MSSQLServerContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MSSQLServerContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/dbScripts/mssql.sql");

    public static ContextRule contextRule = new ContextRule(() -> new String[]{
            "-t", "mssql08",
            "-db", "test",
            "-s", "dbo",
            "-cat", "%",
            "-o", "target/integrationtesting/mssql",
            "-u", "sa",
            "-p", jdbcContainerRule.getContainer().getPassword(),
            "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
            "-port", jdbcContainerRule.getContainer().getMappedPort(1433).toString()
    });

    @ClassRule
    public static final TestRule chain = RuleChain
            .outerRule(jdbcContainerRule)
            .around(contextRule);

    private static Database database;

    @Before
    public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            database = DatabaseCreator.create(contextRule.getContext());
        }
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
