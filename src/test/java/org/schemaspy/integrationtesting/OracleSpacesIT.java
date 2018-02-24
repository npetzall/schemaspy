package org.schemaspy.integrationtesting;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.schemaspy.model.Database;
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

public class OracleSpacesIT {

    public static JdbcContainerRule<OracleContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new OracleContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/oracleSpacesIT/dbScripts/spaces_in_table_names.sql");

    public static TestRule jdbcDriverClassPresentRule = new AssumeClassIsPresentRule("oracle.jdbc.OracleDriver");


    public static ContextRule contextRule = new ContextRule(() -> new String[]{
                "-t", "orathin",
                "-db", jdbcContainerRule.getContainer().getSid(),
                "-s", "ORASPACEIT",
                "-cat", "%",
                "-o", "target/integrationtesting/ORASPACEIT",
                "-u", "oraspaceit",
                "-p", "oraspaceit123",
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
    public void databaseShouldHaveTableWithSpaces() {
        assertThat(database.getTables()).extracting(t -> t.getName()).contains("test 1.0");
    }
}
