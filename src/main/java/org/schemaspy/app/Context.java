package org.schemaspy.app;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.schemaspy.SchemaAnalyzer;
import org.schemaspy.cli.*;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.service.SqlService;
import org.schemaspy.service.TableService;
import org.schemaspy.service.ViewService;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

public class Context implements AutoCloseable {

    private final String contextName;

    private final String[] args;
    private final CommandLineArguments commandLineArguments;
    private final SqlService sqlService;
    private final TableService tableService;
    private final ViewService viewService;
    private final DatabaseService databaseService;
    private final SchemaAnalyzer schemaAnalyzer;

    public Context(String contextName, String...args) {
        this.contextName = contextName;
        this.args = args;
        commandLineArguments = getCommandLineArguments(args);
        if (commandLineArguments.isDebug()) {
            enableDebug();
        }

        sqlService = new SqlService();
        tableService = new TableService(sqlService, commandLineArguments);
        viewService = new ViewService(sqlService);
        databaseService = new DatabaseService(tableService, viewService, sqlService);
        schemaAnalyzer = new SchemaAnalyzer(sqlService, databaseService, commandLineArguments);
    }

    private static CommandLineArguments getCommandLineArguments(String[] args) {
        PropertyFileDefaultProviderFactory propertyFileDefaultProviderFactory = new PropertyFileDefaultProviderFactory();
        ConfigFileArgumentParser configFileArgumentParser = new ConfigFileArgumentParser();
        Optional<String> configFile = configFileArgumentParser.parseConfigFileArgumentValue(args);
        Optional<PropertyFileDefaultProvider> defaultProvider = propertyFileDefaultProviderFactory.create(configFile.orElse(null));
        CommandLineArgumentParser commandLineArgumentParser = new CommandLineArgumentParser(defaultProvider.orElse(null));
        CommandLineArguments commandLineArguments = commandLineArgumentParser.parse(args);
        if (commandLineArguments.isHelpRequired()) {
            commandLineArgumentParser.printUsage();
            System.exit(0);
        }
        if (commandLineArguments.isDbHelpRequired()) {
            commandLineArgumentParser.printDatabaseTypesHelp();
            System.exit(0);
        }
        return commandLineArguments;
    }

    private static void enableDebug() {
        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
        if (iLoggerFactory instanceof LoggerContext) {
            LoggerContext loggerContext = (LoggerContext)iLoggerFactory;
            ch.qos.logback.classic.Logger logger = loggerContext.getLogger("org.schemaspy");
            logger.setLevel(Level.DEBUG);
        }
    }

    public String getContextName() {
        return contextName;
    }

    public String[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }

    public CommandLineArguments getCommandLineArguments() {
        return commandLineArguments;
    }

    public SqlService getSqlService() {
        return sqlService;
    }

    public TableService getTableService() {
        return tableService;
    }

    public ViewService getViewService() {
        return viewService;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public SchemaAnalyzer getSchemaAnalyzer() {
        return schemaAnalyzer;
    }

    @Override
    public void close() {
        SingletonContext.getInstance().removeContext(this);
    }
}
