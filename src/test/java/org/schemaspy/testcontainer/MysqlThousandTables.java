package org.schemaspy.testcontainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Database;
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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MysqlThousandTables {

    @Autowired
    private SqlService sqlService;

    @Autowired
    private DatabaseService databaseService;

    @MockBean
    private CommandLineArguments arguments;

    @MockBean
    private CommandLineRunner commandLineRunner;

    private static Database database;

    @ClassRule
    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MySQLContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitFunctions(connection -> thousandTables(connection));

    private static void thousandTables(Connection connection) {
        try (Statement statement = connection.createStatement()) {

            for (int i = 1; i <= 1000; i++) {
                statement.execute(getTable(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getTable(int i) {
        return "CREATE TABLE groups"+i+" (\n" +
                "  gid INTEGER AUTO_INCREMENT,\n" +
                "  name VARCHAR(16) NOT NULL,\n" +
                "  description VARCHAR(80) NOT NULL,\n" +
                "  PRIMARY KEY (gid),\n" +
                "  UNIQUE name_unique (name)\n" +
                ") engine=InnoDB COMMENT 'Groups"+i+"';";
    }

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException, URISyntaxException {
        String[] args = {
                "-t", "mysql",
                "-db", "test",
                "-s", "test",
                "-cat", "%",
                "-o", "target/integrationtesting/mysql_thousand",
                "-u", "test",
                "-p", "test",
                "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                "-port", jdbcContainerRule.getContainer().getMappedPort(3306).toString()
        };
        given(arguments.getOutputDirectory()).willReturn(new File("target/integrationtesting/mysql_thousand"));
        given(arguments.getDatabaseType()).willReturn("mysql");
        given(arguments.getUser()).willReturn("test");
        given(arguments.getSchema()).willReturn("test");
        given(arguments.getCatalog()).willReturn("%");
        given(arguments.getDatabaseName()).willReturn("test");
        Config config = new Config(args);
        DatabaseMetaData databaseMetaData = sqlService.connect(config);
        Database database = new Database(config, databaseMetaData, arguments.getDatabaseName(), arguments.getCatalog(), arguments.getSchema(), null);
        databaseService.gatheringSchemaDetails(config, database);
        this.database = database;
    }

    @Test
    public void foundAllTables() {
        assertThat(database.getTables().size()).isEqualTo(1000);
    }
}
