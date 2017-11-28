package org.schemaspy.app.testcontainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.app.cli.CommandLineArguments;
import org.schemaspy.input.db.DatabaseService;
import org.schemaspy.input.db.SqlService;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.OracleContainer;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OracleIT {

    @ClassRule
    public static JdbcContainerRule<OracleContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new OracleContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/dbScripts/oracle.sql");

    @Configuration
    @ComponentScan(basePackages = {"org.schemaspy.app.cli", "org.schemaspy.app.config"})
    static class MysqlXMLITConfig {

        @Bean
        public ApplicationArguments applicationArguments() {
            return new DefaultApplicationArguments(new String[]{
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
        }
    }

    @Autowired
    private Config config;

    @Autowired
    private CommandLineArguments arguments;

    @Autowired
    private SqlService sqlService;

    @Autowired
    private DatabaseService databaseService;

    @Mock
    private ProgressListener progressListener;

    private static Database database;

    @Before
    public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            createDatabaseRepresentation();
        }
    }

    private void createDatabaseRepresentation() throws SQLException, IOException, URISyntaxException {
        DatabaseMetaData databaseMetaData = sqlService.connect();
        Database database = new Database(config.getDescription(), databaseMetaData, arguments.getDatabaseName(), arguments.getCatalog(), arguments.getSchema(), null, progressListener);
        databaseService.gatheringSchemaDetails(database, progressListener);
        this.database = database;
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
        return database.getTables().stream().filter(table -> table.getName().equals(tableName)).findFirst().get();
    }
}
