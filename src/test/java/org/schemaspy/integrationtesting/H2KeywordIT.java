package org.schemaspy.integrationtesting;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.app.Context;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.service.SqlService;
import org.schemaspy.testing.ContextRule;
import org.schemaspy.testing.H2MemoryRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class H2KeywordIT {

    @ClassRule
    public static H2MemoryRule h2MemoryRule = new H2MemoryRule("h2keyword").addSqlScript("src/test/resources/integrationTesting/h2KeywordIT/dbScripts/keyword_in_table.sql");

    private static String[] args = {
            "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
            "-db", "h2keyword",
            "-s", "h2keyword",
            "-o", "target/integrationtesting/h2keyword",
            "-u", "sa"
    };

    @ClassRule
    public static ContextRule contextRule = new ContextRule(args);

    private ProgressListener progressListener = mock(ProgressListener.class);

    private static Database database;

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException, URISyntaxException {
        Context context = contextRule.getContext();
        Config config = new Config(args);
        DatabaseMetaData databaseMetaData = context.getSqlService().connect(config);
        CommandLineArguments arguments = context.getCommandLineArguments();
        Database database = new Database(config, databaseMetaData, arguments.getDatabaseName(), arguments.getCatalog(), arguments.getSchema(), null, progressListener);
        context.getDatabaseService().gatheringSchemaDetails(config, database, progressListener);
        this.database = database;
    }

    @Test
    public void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("h2keyword");
    }

    @Test
    public void tableWithKeyWordShouldExist() {
        assertThat(database.getTables()).extracting(t -> t.getName()).contains("DISTINCT");
    }
}
