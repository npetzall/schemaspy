package org.schemaspy.input.dbms;

import org.schemaspy.input.InputSource;
import org.schemaspy.input.dbms.config.DbmsConfiguration;
import org.schemaspy.input.dbms.config.DbmsInputConfiguration;
import org.schemaspy.input.dbms.jdbc.DatabaseService;
import org.schemaspy.input.dbms.jdbc.SqlService;
import org.schemaspy.input.dbms.jdbc.TableService;
import org.schemaspy.input.dbms.jdbc.ViewService;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;

import java.sql.SQLException;

public class DbmsInputSource implements InputSource{

    private final DbmsConfiguration dbmsConfiguration;
    private final ProgressListener progressListener;
    private final SqlService sqlService;
    private final ViewService viewService;
    private final TableService tableService;
    private final DatabaseService databaseService;

    public DbmsInputSource(DbmsInputConfiguration dbmsInputConfiguration, ProgressListener progressListener) {
        dbmsConfiguration = DbmsConfiguration.from(dbmsInputConfiguration);
        this.progressListener = progressListener;
        sqlService = new SqlService();
        viewService = new ViewService(sqlService);
        tableService = new TableService(dbmsConfiguration, sqlService);
        databaseService = new DatabaseService(dbmsConfiguration, tableService, viewService, sqlService);
    }

    @Override
    public Database createDatabaseModel() {
        String databaseProductName = sqlService.getDatabaseProductName();
        Database database = new Database(
                databaseProductName,
                dbmsConfiguration.getDatabaseName(),
                dbmsConfiguration.getCatalog(),
                dbmsConfiguration.getSchema(),
                dbmsConfiguration.getSchemaMeta()
        );
        try {
            databaseService.gatheringSchemaDetails(database, progressListener);
        } catch (SQLException e) {
            throw new DbmsInputException("Failed to read database from dbms", e);
        }
        return database;
    }

}
