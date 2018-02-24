package org.schemaspy.testcontainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.schemaspy.model.*;
import org.schemaspy.testing.ContextRule;
import org.schemaspy.testing.DatabaseCreator;
import org.testcontainers.containers.MySQLContainer;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

public class MysqlSpacesNoDotsIT {

    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MySQLContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/dbScripts/mysql_spaces_no_dots.sql")
                    .withInitUser("root", "test");

    public static ContextRule contextRule = new ContextRule(() -> new String[]{
            "-t", "mysql",
            "-db", "TEST 1",
            "-s", "TEST 1",
            "-cat", "%",
            "-o", "target/integrationtesting/mysql_spaces_no_dots",
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
    public void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("TEST 1");
    }

    @Test
    public void databaseShouldHaveTable() {
        assertThat(database.getTables()).extracting(Table::getName).contains("TABLE 1");
    }

    @Test
    public void tableShouldHavePKWithAutoIncrement() {
        assertThat(database.getTablesByName().get("TABLE 1").getColumns()).extracting(TableColumn::getName).contains("id");
        assertThat(database.getTablesByName().get("TABLE 1").getColumn("id").isPrimary()).isTrue();
        assertThat(database.getTablesByName().get("TABLE 1").getColumn("id").isAutoUpdated()).isTrue();
    }

    @Test
    public void tableShouldHaveForeignKey() {
        assertThat(database.getTablesByName().get("TABLE 1").getForeignKeys()).extracting(ForeignKeyConstraint::getName).contains("link fk");
    }

    @Test
    public void tableShouldHaveUniqueKey() {
        assertThat(database.getTablesByName().get("TABLE 1").getIndexes()).extracting(TableIndex::getName).contains("name_link_unique");
    }

    @Test
    public void tableShouldHaveColumnWithSpaceInIt() {
        assertThat(database.getTablesByName().get("TABLE 1").getColumns()).extracting(TableColumn::getName).contains("link id");
    }
}
