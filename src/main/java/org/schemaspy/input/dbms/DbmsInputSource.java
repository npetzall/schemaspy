package org.schemaspy.input.dbms;

import org.schemaspy.Config;
import org.schemaspy.DbAnalyzer;
import org.schemaspy.input.InputSource;
import org.schemaspy.input.dbms.config.DbmsConfiguration;
import org.schemaspy.input.dbms.config.DbmsConfigurationFactory;
import org.schemaspy.input.dbms.config.DbmsInputConfiguration;
import org.schemaspy.input.dbms.jdbc.*;
import org.schemaspy.model.Database;
import org.schemaspy.model.EmptySchemaException;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class DbmsInputSource implements InputSource{

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final DbmsInputConfiguration dbmsInputConfiguration;
    private final ProgressListener progressListener;

    public DbmsInputSource(DbmsInputConfiguration dbmsInputConfiguration, ProgressListener progressListener) {
        this.dbmsInputConfiguration = dbmsInputConfiguration;
        this.progressListener = progressListener;
    }

    @Override
    public Database createDatabaseModel() {
        DbmsConfiguration dbmsConfiguration = new DbmsConfigurationFactory().createDbmsConfiguration(dbmsInputConfiguration);
        DriverFactory driverFactory = new DriverFactory(dbmsConfiguration.getDbmsDriverConfiguration());
        ConnectionFactory connectionFactory = new ConnectionFactory(dbmsConfiguration.getDbmsConnectionConfiguration(), driverFactory.createDriver());
        SqlService sqlService;
        try {
            sqlService = new SqlServiceFactory().createSqlService(connectionFactory.getConnection());
        } catch (SQLException e) {
            throw new DbmsInputException("Failed to create SqlService", e);
        }
        ViewService viewService = new ViewService(sqlService);
        TableService tableService = new TableService(dbmsConfiguration, sqlService);
        DatabaseService databaseService = new DatabaseService(dbmsConfiguration, tableService, viewService, sqlService);
        Database database = new DatabaseFactory().createDatabase(dbmsConfiguration, sqlService);
        try {
            databaseService.gatheringSchemaDetails(database, progressListener);
        } catch (SQLException e) {
            throw new DbmsInputException("Failed to read database from dbms", e);
        }
        Collection<Table> tables = new ArrayList<>(database.getTables());
        tables.addAll(database.getViews());

        if (tables.isEmpty()) {
            dumpNoTablesMessage(dbmsConfiguration.getSchema(), dbmsConfiguration.getUser(), sqlService.getDatabaseMetaData(), dbmsConfiguration.getTableInclusions() != null);
            if (!Config.getInstance().isOneOfMultipleSchemas()) // don't bail if we're doing the whole enchilada
                throw new EmptySchemaException();
        }
        return database;
    }

    /**
     * dumpNoDataMessage
     *
     * @param schema String
     * @param user   String
     * @param meta   DatabaseMetaData
     */
    private static void dumpNoTablesMessage(String schema, String user, DatabaseMetaData meta, boolean specifiedInclusions) {
        LOGGER.warn("No tables or views were found in schema '{}'.", schema);
        List<String> schemas;
        try {
            schemas = DbAnalyzer.getSchemas(meta);
        } catch (SQLException | RuntimeException exc) {
            LOGGER.error("The user you specified '{}' might not have rights to read the database metadata.", user, exc);
            return;
        }

        if (Objects.isNull(schemas)) {
            LOGGER.error("Failed to retrieve any schemas");
            return;
        } else if (schemas.contains(schema)) {
            LOGGER.error(
                    "The schema exists in the database, but the user you specified '{}'" +
                            "might not have rights to read its contents.",
                    user);
            if (specifiedInclusions) {
                LOGGER.error(
                        "Another possibility is that the regular expression that you specified " +
                                "for what to include (via -i) didn't match any tables.");
            }
        } else {
            LOGGER.error(
                    "The schema '{}' could not be read/found, schema is specified using the -s option." +
                            "Make sure user '{}' has the correct privileges to read the schema." +
                            "Also not that schema names are usually case sensitive.",
                    schema, user);
            LOGGER.info(
                    "Available schemas(Some of these may be user or system schemas):" +
                            System.lineSeparator() + "{}",
                    schemas.stream().collect(Collectors.joining(System.lineSeparator())));
            List<String> populatedSchemas = Collections.emptyList();
            try {
                populatedSchemas = DbAnalyzer.getPopulatedSchemas(meta);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (populatedSchemas.isEmpty()) {
                LOGGER.error("Unable to determine if any of the schemas contain tables/views");
            } else {
                LOGGER.info("Schemas with tables/views visible to '{}':" + System.lineSeparator() + "{}",
                        populatedSchemas.stream().collect(Collectors.joining(System.lineSeparator())));
            }
        }
    }

}
