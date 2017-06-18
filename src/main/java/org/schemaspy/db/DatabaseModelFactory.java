package org.schemaspy.db;

import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.xml.SchemaMeta;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.service.SqlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@Component
public class DatabaseModelFactory {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseModelFactory.class);

    private final SqlService sqlService;

    private final DatabaseService databaseService;

    private final CommandLineArguments commandLineArguments;

    public DatabaseModelFactory(SqlService sqlService, DatabaseService databaseService, CommandLineArguments commandLineArguments) {
        this.sqlService = sqlService;
        this.databaseService = databaseService;
        this.commandLineArguments = commandLineArguments;
    }

    public Database createDatabaseModel(String schema, Config config, ProgressListener progressListener) throws IOException, SQLException {

        String dbName = commandLineArguments.getDatabaseName();

        String catalog = commandLineArguments.getCatalog();

        DatabaseMetaData meta = sqlService.connect(config);

        logger.debug("supportsSchemasInTableDefinitions: " + meta.supportsSchemasInTableDefinitions());
        logger.debug("supportsCatalogsInTableDefinitions: " + meta.supportsCatalogsInTableDefinitions());

        // set default Catalog and Schema of the connection
        if(schema == null)
            schema = meta.getConnection().getSchema();
        if(catalog == null)
            catalog = meta.getConnection().getCatalog();

        SchemaMeta schemaMeta = config.getMeta() == null ? null : new SchemaMeta(config.getMeta(), dbName, schema);

        logger.info("Connected to " + meta.getDatabaseProductName() + " - " + meta.getDatabaseProductVersion());

        if (schemaMeta != null && schemaMeta.getFile() != null) {
            logger.info("Using additional metadata from " + schemaMeta.getFile());
        }

        Database db = new Database(config, meta, dbName, catalog, schema, schemaMeta, progressListener);
        databaseService.gatheringSchemaDetails(config, db, progressListener);
        return db;
    }
}
