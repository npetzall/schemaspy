/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011 John Currier
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy.app;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.schemaspy.Config;
import org.schemaspy.analyzer.DbAnalyzer;
import org.schemaspy.app.cli.CommandLineArguments;
import org.schemaspy.input.db.ConnectionURLBuilder;
import org.schemaspy.input.db.DatabaseService;
import org.schemaspy.input.db.DbDriverLoader;
import org.schemaspy.input.db.SqlService;
import org.schemaspy.model.*;
import org.schemaspy.model.xml.SchemaMeta;
import org.schemaspy.output.html.HtmlProducer;
import org.schemaspy.output.html.dot.HtmlMultipleSchemasIndexPage;
import org.schemaspy.output.html.dot.MustacheCatalog;
import org.schemaspy.output.html.dot.MustacheSchema;
import org.schemaspy.output.html.dot.ResourceWriter;
import org.schemaspy.output.text.insertdeleteorder.InsertDeleteOrderProducer;
import org.schemaspy.output.xml.XmlProducer;
import org.schemaspy.util.LogFormatter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

//import javax.xml.validation.Schema;

/**
 * @author John Currier
 */

public class SchemaAnalyzer {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private boolean fineEnabled;

    private final SqlService sqlService;

    private final DatabaseService databaseService;

    private final CommandLineArguments commandLineArguments;

    private final Config config;

    private final ProgressListener progressListener;

    private final HtmlProducer htmlProducer;
    private final XmlProducer xmlProducer;
    private final InsertDeleteOrderProducer insertDeleteOrderProducer;

    public SchemaAnalyzer(SqlService sqlService, DatabaseService databaseService, CommandLineArguments commandLineArguments, Config config, ProgressListener progressListener, HtmlProducer htmlProducer, XmlProducer xmlProducer, InsertDeleteOrderProducer insertDeleteOrderProducer) {
        this.sqlService = Objects.requireNonNull(sqlService);
        this.databaseService = Objects.requireNonNull(databaseService);
        this.commandLineArguments = Objects.requireNonNull(commandLineArguments);
        this.config = Objects.requireNonNull(config);
        this.progressListener = Objects.requireNonNull(progressListener);
        this.htmlProducer = Objects.requireNonNull(htmlProducer);
        this.xmlProducer = Objects.requireNonNull(xmlProducer);
        this.insertDeleteOrderProducer = Objects.requireNonNull(insertDeleteOrderProducer);
    }

    public Database analyze() throws SQLException, IOException {
        // don't render console-based detail unless we're generating HTML (those probably don't have a user watching)
    	// and not already logging fine details (to keep from obfuscating those)

    	// if -all(evaluteAll) or -schemas given then analyzeMultipleSchemas  
        List<String> schemas = config.getSchemas();
        if (schemas != null || config.isEvaluateAllEnabled()) {
            return this.analyzeMultipleSchemas();
        } else {
            File outputDirectory = commandLineArguments.getOutputDirectory();
            Objects.requireNonNull(outputDirectory);
            String schema = commandLineArguments.getSchema();
            return analyze(schema, outputDirectory);
        }
    }

    public Database analyzeMultipleSchemas() throws SQLException, IOException {
        try {
            // following params will be replaced by something appropriate
            List<String> args = config.asList();
            args.remove("-schemas");
            args.remove("-schemata");
           
            List<String> schemas = config.getSchemas();
            Database db = null;
        	String schemaSpec = config.getSchemaSpec();
            Connection connection = this.getConnection();
            DatabaseMetaData meta = connection.getMetaData();
            //-all(evaluteAll) given then get list of the database schemas
            if (schemas == null || config.isEvaluateAllEnabled()) {
            	if(schemaSpec==null)
            		schemaSpec=".*";
                System.out.println("Analyzing schemas that match regular expression '" + schemaSpec + "':");
                System.out.println("(use -schemaSpec on command line or in .properties to exclude other schemas)");
                schemas = DbAnalyzer.getPopulatedSchemas(meta, schemaSpec, false);
                if (schemas.isEmpty())
                	schemas = DbAnalyzer.getPopulatedSchemas(meta, schemaSpec, true);
                if (schemas.isEmpty())
                	schemas = Arrays.asList(new String[] {config.getUser()});
            }

        	System.out.println("Analyzing schemas: "+schemas.toString());
        	
	        String dbName = config.getDb();
	        File outputDir = commandLineArguments.getOutputDirectory();
	        // set flag which later on used for generation rootPathtoHome link.
	        //TODO Mutating config BAD
            config.setOneOfMultipleSchemas(true);

	        List<MustacheSchema> mustacheSchemas =new ArrayList<MustacheSchema>();
            MustacheCatalog mustacheCatalog = null;
            for (String schema : schemas) {
	        	// reset -all(evaluteAll) and -schemas parametter to avoid infinite loop! now we are analyzing single schema
                //TODO Mutating config BAD
                config.setSchemas(null);
                //TODO Mutating config BAD
	            config.setEvaluateAllEnabled(false);
	            if (dbName == null)
                    //TODO Mutating config BAD
	            	config.setDb(schema);
	            else
                    //TODO Mutating config BAD
	        		config.setSchema(schema);

                System.out.println("Analyzing " + schema);
                System.out.flush();
                File outputDirForSchema = new File(outputDir, schema);
                db = this.analyze(schema, outputDirForSchema);
                if (db == null) //if any of analysed schema returns null
                    return db;
                mustacheSchemas.add(new MustacheSchema(db.getSchema(),""));
                //TODO will only be set by the last ? Why set in every loop?
                mustacheCatalog = new MustacheCatalog(db.getCatalog(), "");
	        }
            //TODO Will generate HTML regardless of -nohtml
            prepareLayoutFiles(outputDir);
            HtmlMultipleSchemasIndexPage.getInstance().write(outputDir, dbName, mustacheCatalog ,mustacheSchemas, meta);
	        
	        return db;
        } catch (Config.MissingRequiredParameterException missingParam) {
            config.dumpUsage(missingParam.getMessage(), missingParam.isDbTypeSpecific());
            return null;
        }
    }

    public Database analyze(String schema, File outputDir) throws SQLException, IOException {
        try {
            // set the log level for the root logger
            Logger.getLogger("").setLevel(config.getLogLevel());

            // clean-up console output a bit
            for (Handler handler : Logger.getLogger("").getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    ((ConsoleHandler)handler).setFormatter(new LogFormatter());
                    handler.setLevel(config.getLogLevel());
                }
            }

            fineEnabled = logger.isLoggable(Level.FINE);
            logger.info("Starting schema analysis");

            if (!outputDir.isDirectory()) {
                if (!outputDir.mkdirs()) {
                    throw new IOException("Failed to create directory '" + outputDir + "'");
                }
            }

            String dbName = config.getDb();

            String catalog = commandLineArguments.getCatalog();

            DatabaseMetaData meta = sqlService.connect();

            logger.fine("supportsSchemasInTableDefinitions: " + meta.supportsSchemasInTableDefinitions());
            logger.fine("supportsCatalogsInTableDefinitions: " + meta.supportsCatalogsInTableDefinitions());

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
            //
            // create our representation of the database
            //
            Database db = new Database(config.getDescription(), meta, dbName, catalog, schema, schemaMeta, progressListener);
            databaseService.gatheringSchemaDetails(db, progressListener);

            long duration = progressListener.startedGraphingSummaries();
            schemaMeta = null; // done with it so let GC reclaim it

            Collection<Table> tables = new ArrayList<Table>(db.getTables());
            tables.addAll(db.getViews());

            if (db.getTables().isEmpty() && db.getViews().isEmpty()) {
                dumpNoTablesMessage(schema, config.getUser(), meta, config.getTableInclusions() != null);
                if (!config.isOneOfMultipleSchemas()) // don't bail if we're doing the whole enchilada
                    throw new EmptySchemaException();
            }

            if (config.isHtmlGenerationEnabled()) {
                htmlProducer.generate(db);
            }

            xmlProducer.generate(db, outputDir);

            insertDeleteOrderProducer.generate(db);

            duration = progressListener.finishedGatheringDetails();
            long overallDuration = progressListener.finished(tables, config);

            if (config.isHtmlGenerationEnabled()) {
                logger.info("Wrote table details in " + duration / 1000 + " seconds");

                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Wrote relationship details of " + tables.size() + " tables/views to directory '" + outputDir + "' in " + overallDuration / 1000 + " seconds.");
                    logger.info("View the results by opening " + new File(outputDir, "index.html"));
                }
            }

            return db;
        } catch (Config.MissingRequiredParameterException missingParam) {
            config.dumpUsage(missingParam.getMessage(), missingParam.isDbTypeSpecific());
            return null;
        }
    }

    /**
     * This method is responsible to copy layout folder to destination directory and not copy template .html files
     * @param outputDir
     * @throws IOException
     */
    @Deprecated
    private void prepareLayoutFiles(File outputDir) throws IOException {
        URL url = getClass().getResource("/layout");
        File directory = new File(url.getPath());

        IOFileFilter notHtmlFilter = FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(".html"));
        FileFilter filter = FileFilterUtils.and(notHtmlFilter);
        //cleanDirectory(outputDir,"/diagrams");
        //cleanDirectory(outputDir,"/tables");
        ResourceWriter.copyResources(url, outputDir, filter);
    }

    private Connection getConnection() throws InvalidConfigurationException, IOException {

        Properties properties = config.determineDbProperties(commandLineArguments.getDatabaseType());

        ConnectionURLBuilder urlBuilder = new ConnectionURLBuilder(config, properties);
        if (config.getDb() == null)
            //TODO Mutating config BAD
            config.setDb(urlBuilder.build());

        String driverClass = properties.getProperty("driver");
        String driverPath = properties.getProperty("driverPath");
        if (driverPath == null)
            driverPath = "";
        if (config.getDriverPath() != null)
            driverPath = config.getDriverPath() + File.pathSeparator + driverPath;

        DbDriverLoader driverLoader = new DbDriverLoader();
        return driverLoader.getConnection(config, urlBuilder.build(), driverClass, driverPath);
    }

    /**
     * dumpNoDataMessage
     *
     * @param schema String
     * @param user String
     * @param meta DatabaseMetaData
     */
    private static void dumpNoTablesMessage(String schema, String user, DatabaseMetaData meta, boolean specifiedInclusions) throws SQLException {
        System.out.println();
        System.out.println();
        System.out.println("No tables or views were found in schema '" + schema + "'.");
        List<String> schemas = null;
        Exception failure = null;
        try {
            schemas = DbAnalyzer.getSchemas(meta);
        } catch (SQLException exc) {
            failure = exc;
        } catch (RuntimeException exc) {
            failure = exc;
        }

        if (schemas == null) {
            System.out.println("The user you specified (" + user + ')');
            System.out.println("  might not have rights to read the database metadata.");
            System.out.flush();
            if (failure != null)    // to appease the compiler
                failure.printStackTrace();
            return;
        } else if (schema == null || schemas.contains(schema)) {
            System.out.println("The schema exists in the database, but the user you specified (" + user + ')');
            System.out.println("  might not have rights to read its contents.");
            if (specifiedInclusions) {
                System.out.println("Another possibility is that the regular expression that you specified");
                System.out.println("  for what to include (via -i) didn't match any tables.");
            }
        } else {
            System.out.println("The schema does not exist in the database.");
            System.out.println("Make sure that you specify a valid schema with the -s option and that");
            System.out.println("  the user specified (" + user + ") can read from the schema.");
            System.out.println("Note that schema names are usually case sensitive.");
        }
        System.out.println();
        boolean plural = schemas.size() != 1;
        System.out.println(schemas.size() + " schema" + (plural ? "s" : "") + " exist" + (plural ? "" : "s") + " in this database.");
        System.out.println("Some of these \"schemas\" may be users or system schemas.");
        System.out.println();
        for (String unknown : schemas) {
            System.out.print(unknown + " ");
        }

        System.out.println();
        List<String> populatedSchemas = DbAnalyzer.getPopulatedSchemas(meta);
        if (populatedSchemas.isEmpty()) {
            System.out.println("Unable to determine if any of the schemas contain tables/views");
        } else {
            System.out.println("These schemas contain tables/views that user '" + user + "' can see:");
            System.out.println();
            for (String populated : populatedSchemas) {
                System.out.print(" " + populated);
            }
        }
    }
}
