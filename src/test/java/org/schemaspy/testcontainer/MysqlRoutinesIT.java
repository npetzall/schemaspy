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

import java.io.IOException;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

public class MysqlRoutinesIT {

    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MySQLContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/mysqlroutines/dbScripts/routines.sql");

    public static ContextRule contextRule = new ContextRule(() -> new String[]{
            "-t", "mysql",
            "-db", "test",
            "-s", "test",
            "-cat", "%",
            "-o", "target/integrationtesting/mysqlroutines",
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
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException {
        if (database == null) {
            database = DatabaseCreator.create(contextRule.getContext());
        }
    }

    @Test
    public void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("test");
    }

    @Test
    public void databaseShouldHaveRoutines() {
        assertThat(database.getRoutinesMap().get("no_det").isDeterministic()).isFalse();
        assertThat(database.getRoutinesMap().get("yes_det").isDeterministic()).isTrue();
    }
}
