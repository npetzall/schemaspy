package org.schemaspy.testcontainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.schemaspy.model.Database;
import org.schemaspy.testing.ContextRule;
import org.schemaspy.testing.DatabaseCreator;
import org.testcontainers.containers.MySQLContainer;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

public class MysqlKeyWordTableIT {

    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MySQLContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/dbScripts/mysql_keyword.sql");

    public static ContextRule contextRule = new ContextRule(() -> new String[]{
            "-t", "mysql",
            "-db", "test",
            "-s", "test",
            "-cat", "%",
            "-o", "target/integrationtesting/mysql_keywords",
            "-u", "test",
            "-p", "test",
            "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
            "-port", jdbcContainerRule.getContainer().getMappedPort(3306).toString()
    });

    @ClassRule
    public static final TestRule chain = RuleChain
            .outerRule(jdbcContainerRule)
            .around(contextRule);

    private static Database database;

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            database = DatabaseCreator.create(contextRule.getContext());
        }
    }

    @Test
    public void hasATableNamedDistinct() {
        assertThat(database.getTables()).extracting(t -> t.getName()).contains("DISTINCT");
    }
}
