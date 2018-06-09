package org.schemaspy.testcontainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.service.SqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MySQLContainer;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MysqlSchemaLeakageIT {

    @Autowired
    private SqlService sqlService;

    @Autowired
    private DatabaseService databaseService;

    @Mock
    private ProgressListener progressListener;

    @MockBean
    private CommandLineArguments arguments;

    @MockBean
    private CommandLineRunner commandLineRunner;

    private static Database database;

    @ClassRule
    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MySQLContainer("mysql:5"))
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/mysql_schema_leakage/dbScripts/mysql_table_view_collision.sql")
                    .withInitUser("root", "test");

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException, URISyntaxException {
        String[] args = {
                "-t", "mysql",
                "-db", "test1",
                "-s", "test1",
                "-cat", "%",
                "-o", "target/integrationtesting/mysql_schema_leakage",
                "-u", "testUser",
                "-p", "password",
                "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                "-port", jdbcContainerRule.getContainer().getMappedPort(3306).toString()
        };
        given(arguments.getOutputDirectory()).willReturn(new File("target/integrationtesting/mysql_schema_leakage"));
        given(arguments.getDatabaseType()).willReturn("mysql");
        given(arguments.getUser()).willReturn("testUser");
        given(arguments.getSchema()).willReturn("test1");
        given(arguments.getCatalog()).willReturn("%");
        given(arguments.getDatabaseName()).willReturn("test1");
        Config config = new Config(args);
        sqlService.connect(config);
        Database database = new Database(
                sqlService.getDbmsMeta(),
                arguments.getDatabaseName(),
                arguments.getCatalog(),
                arguments.getSchema()
        );
        databaseService.gatheringSchemaDetails(config, database, null, progressListener);
        this.database = database;
    }

    @Test
    public void whatHaveItGot() {
        assertThat(database.getViews().size()).isEqualTo(0);
    }
}