package org.schemaspy.input.dbms.config;

import org.schemaspy.model.xml.SchemaMeta;

import java.util.Properties;
import java.util.regex.Pattern;

public class DbmsConfiguration {

    private final DbmsInputConfiguration dbmsInputConfiguration;
    private final Properties properties;
    private final int maxDbThreads;
    private final String[] tableTypes;
    private final String[] viewTypes;
    private final SchemaMeta schemaMeta;

    public DbmsConfiguration(DbmsInputConfiguration dbmsInputConfiguration, Properties properties) {

    }

    public DbmsSql getDbmsSql() {
        return new DbmsSql(properties);
    }

    public SchemaMeta getSchemaMeta() {
        return schemaMeta;
    }

    public boolean isViewsEnabled(){
        return dbmsInputConfiguration.isViewsEnabled();
    }

    public Pattern getTableInclusions() {
        return dbmsInputConfiguration.getTableInclusions();
    }

    public Pattern getTableExclusions() {
        return dbmsInputConfiguration.getTableExclusions();
    }

    public int getMaxDbThreads() {
        return maxDbThreads;
    }

    public String[] getTableTypes() {
        return tableTypes;
    }

    public String[] getViewTypes() {
        return viewTypes;
    }

    public Pattern getIndirectColumnExclusions() {
        return dbmsInputConfiguration.getIndirectColumnExclusions();
    }

    public Pattern getColumnExclusions() {
        return dbmsInputConfiguration.getColumnExclusions();
    }

    public String getDatabaseName() {
        return dbmsInputConfiguration.getDatabaseName();
    }

    public String getCatalog() {
        return dbmsInputConfiguration.getCatalog();
    }

    public String getSchema() {
        return dbmsInputConfiguration.getSchema();
    }

}
