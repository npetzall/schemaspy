package org.schemaspy.input.dbms.config;

import org.schemaspy.model.xml.SchemaMeta;

import java.util.regex.Pattern;

public class DbmsConfiguration {

    private final DbmsInputConfiguration dbmsInputConfiguration;
    private final DbmsDriverConfiguration dbmsDriverConfiguration;
    private final DbmsConnectionConfiguration dbmsConnectionConfiguration;
    private final int maxDbThreads;
    private final DbmsSql dbmsSql;
    private final SchemaMeta schemaMeta;

    public DbmsConfiguration(
            DbmsInputConfiguration dbmsInputConfiguration,
            DbmsDriverConfiguration dbmsDriverConfiguration,
            DbmsConnectionConfiguration dbmsConnectionConfiguration,
            int maxDbThreads,
            DbmsSql dbmsSql,
            SchemaMeta schemaMeta
    ) {
        this.dbmsInputConfiguration = dbmsInputConfiguration;
        this.dbmsDriverConfiguration = dbmsDriverConfiguration;
        this.dbmsConnectionConfiguration = dbmsConnectionConfiguration;
        this.maxDbThreads = maxDbThreads;
        this.dbmsSql = dbmsSql;
        this.schemaMeta = schemaMeta;
    }

    public DbmsDriverConfiguration getDbmsDriverConfiguration() {
        return dbmsDriverConfiguration;
    }

    public DbmsConnectionConfiguration getDbmsConnectionConfiguration() {
        return dbmsConnectionConfiguration;
    }

    public DbmsSql getDbmsSql() {
        return dbmsSql;
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

    public boolean isNumRowsEnabled() {
        return dbmsInputConfiguration.isNumRowsEnabled();
    }

    public String getUser() {
        return dbmsInputConfiguration.getUser();
    }
}
