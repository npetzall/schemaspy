package org.schemaspy.input.dbms.config;

import org.schemaspy.model.xml.SchemaMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class DbmsConfigurationFactory {

    private final PropertiesResolver propertiesResolver;

    public DbmsConfigurationFactory() {
        this.propertiesResolver = new PropertiesResolver();
    }

    public DbmsConfigurationFactory(PropertiesResolver propertiesResolver) {
        this.propertiesResolver = propertiesResolver;
    }

    public DbmsConfiguration createDbmsConfiguration(DbmsInputConfiguration dbmsInputConfiguration) {
        Properties dbProperties =  propertiesResolver.getDbProperties(dbmsInputConfiguration.getDatabaseType());
        int maxDbThreads = computeMaxDbThreads(dbProperties, dbmsInputConfiguration);
        String[] tableTypes = getTypes(dbProperties,"tableTypes", "TABLE");
        String[] viewTypes = getTypes(dbProperties,"viewTypes", "VIEW");
        SchemaMeta schemaMeta = createSchemaMeta(dbmsInputConfiguration);
        DbmsSql dbmsSql = new DbmsSql(dbProperties, tableTypes, viewTypes);
        DbmsDriverConfiguration dbmsDriverConfiguration = createDriverConfiguration(dbProperties, dbmsInputConfiguration);
        DbmsConnectionConfiguration dbmsConnectionConfiguration = createConnectionConfiguration(dbProperties, dbmsInputConfiguration);
        return new DbmsConfiguration(
                dbmsInputConfiguration,
                dbmsDriverConfiguration,
                dbmsConnectionConfiguration,
                maxDbThreads,
                dbmsSql,
                schemaMeta
        );
    }

    private int computeMaxDbThreads(Properties properties, DbmsInputConfiguration dbmsInputConfiguration) {
        int max = 1;
        int maxFromProperties = maxDbThreadsFromProperties(properties);
        if (maxFromProperties > 0) {
            max = maxFromProperties;
            int maxFromInput = dbmsInputConfiguration.getMaxDbThreads();
            if (maxFromInput > 0)
                max = maxFromInput;
        }
        return max;
    }

    private int maxDbThreadsFromProperties(Properties properties) {
        String threads = properties.getProperty("dbThreads");
        if (threads == null) {
            threads = properties.getProperty("dbthreads");
        }
        return parseInt(threads);
    }

    private int parseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private String[] getTypes(Properties properties, String propName, String defaultValue) {
        String value = properties.getProperty(propName, defaultValue);
        List<String> types = new ArrayList<>();
        for (String type : value.split(",")) {
            type = type.trim();
            if (type.length() > 0)
                types.add(type);
        }

        return types.toArray(new String[types.size()]);
    }


    private SchemaMeta createSchemaMeta(DbmsInputConfiguration dbmsInputConfiguration) {
        String schemaMetaPath = dbmsInputConfiguration.getSchemaMetaPath();
        if (Objects.isNull(schemaMetaPath)) {
            return null;
        }
        return new SchemaMeta(schemaMetaPath, dbmsInputConfiguration.getDatabaseName(), dbmsInputConfiguration.getSchema());
    }

    private DbmsDriverConfiguration createDriverConfiguration(Properties dbProperties, DbmsInputConfiguration dbmsInputConfiguration) {
        String driverPath = dbmsInputConfiguration.getDriverPath();
        if (Objects.isNull(driverPath) || driverPath.isEmpty()) {
            driverPath = dbProperties.getProperty("driverPath", "");
        }
        String driverClass = dbProperties.getProperty("driver");
        return new DbmsDriverConfiguration(driverPath, driverClass);
    }

    private DbmsConnectionConfiguration createConnectionConfiguration(Properties dbProperties, DbmsInputConfiguration dbmsInputConfiguration) {
        String connectionUrl = new ConnectionUrlBuilder()
                .usingDatabaseProperties(dbProperties)
                .usingArguments(dbmsInputConfiguration.getArguments())
                .withArgument("db", dbmsInputConfiguration.getDatabaseName())
                .withArgument("host", dbmsInputConfiguration.getHost())
                .withArgument("port", dbmsInputConfiguration.getPort())
                .withArgument("user", dbmsInputConfiguration.getUser())
                .withArgument("password", dbmsInputConfiguration.getPassword())
                .build();
        Properties connectionProperties = new Properties(dbmsInputConfiguration.getConnectionProperties());
        if (Objects.nonNull(dbmsInputConfiguration.getUser())) {
            connectionProperties.setProperty("user", dbmsInputConfiguration.getUser());
        }
        if (Objects.nonNull(dbmsInputConfiguration.getPassword())) {
            connectionProperties.setProperty("password", dbmsInputConfiguration.getPassword());
        }
        return new DbmsConnectionConfiguration(connectionUrl, connectionProperties);
    }
}
