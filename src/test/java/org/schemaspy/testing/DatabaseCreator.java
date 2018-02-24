package org.schemaspy.testing;

import org.schemaspy.Config;
import org.schemaspy.app.Context;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.mockito.Mockito.mock;

public class DatabaseCreator {

    private static final ProgressListener progressListener = mock(ProgressListener.class);

    public static Database create(Context context) throws IOException, SQLException {
        CommandLineArguments arguments = context.getCommandLineArguments();
        Config config = new Config(context.getArgs());
        DatabaseMetaData databaseMetaData = context.getSqlService().connect(config);
        Database database = new Database(config, databaseMetaData, arguments.getDatabaseName(), arguments.getCatalog(), arguments.getSchema(), null, progressListener);
        context.getDatabaseService().gatheringSchemaDetails(config, database, progressListener);
        return database;
    }
}
