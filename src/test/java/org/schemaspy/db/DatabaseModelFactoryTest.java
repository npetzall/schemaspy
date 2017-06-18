package org.schemaspy.db;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.testing.H2MemoryRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DatabaseModelFactoryTest {

    private static String CREATE_SCHEMA = "CREATE SCHEMA DATABASESERVICEIT AUTHORIZATION SA";
    private static String SET_SCHEMA = "SET SCHEMA DATABASESERVICEIT";
    private static String CREATE_TABLE = "CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))";

    @ClassRule
    public static H2MemoryRule h2MemoryRule = new H2MemoryRule("DatabaseServiceIT", CREATE_SCHEMA, SET_SCHEMA, CREATE_TABLE);

    @Autowired
    private DatabaseModelFactory databaseModelFactory;

    @Mock
    private ProgressListener progressListener;

    @MockBean
    private CommandLineArguments arguments;

    @MockBean
    private CommandLineRunner commandLineRunner;

    private Database database;

    @Before
    public synchronized void createDatabaseModel() throws Exception {
        if (database == null) {
            String[] args = {
                    "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
                    "-db", "DatabaseServiceIT",
                    "-s", "DATABASESERVICEIT",
                    "-o", "target/integrationtesting/databaseServiceIT",
                    "-u", "sa"
            };
            given(arguments.getOutputDirectory()).willReturn(new File("target/integrationtesting/databaseServiceIT"));
            given(arguments.getDatabaseType()).willReturn("src/test/resources/integrationTesting/dbTypes/h2memory");
            given(arguments.getUser()).willReturn("sa");
            given(arguments.getSchema()).willReturn("DATABASESERVICEIT");
            given(arguments.getDatabaseName()).willReturn("DatabaseServiceIT");

            Config config = new Config(args);
            database = databaseModelFactory.createDatabaseModel(arguments.getSchema(), config, progressListener);
        }
    }

    @Test
    public void verifyCorrectNumberOfTables() {
        assertThat(database.getTables()).hasSize(1);
    }

}