package org.schemaspy.input.dbms.testing;

import org.schemaspy.input.dbms.config.DbmsInputConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class DbmsInputConfigurationFluent implements DbmsInputConfiguration {

    private String databaseType;
    private String host;
    private String port;
    private String user;
    private String password;
    private String driverPath;
    private Properties connectionProperties;
    private Map<String,String> connectionProps;

    private String databaseName;
    private String catalog;
    private String schema;
    private String schemaMetaPath;

    private int maxDbThreads;
    private boolean noViews = false;
    private boolean noRows = true;

    private Pattern tableInclusions = Pattern.compile(".*");
    private Pattern tableExclusions = Pattern.compile("");
    private Pattern columnExclusions = Pattern.compile("[^.]");
    private Pattern indirectColumnExclusions = Pattern.compile("[^.]");

    private Map<String,String> arguments = new HashMap<>();

    public String getDatabaseType() {
        return databaseType;
    }

    public DbmsInputConfigurationFluent setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
        return this;
    }

    public String getHost() {
        return host;
    }

    public DbmsInputConfigurationFluent setHost(String host) {
        this.host = host;
        return this;
    }

    public String getPort() {
        return port;
    }

    public DbmsInputConfigurationFluent setPort(String port) {
        this.port = port;
        return this;
    }

    public String getUser() {
        return user;
    }

    public DbmsInputConfigurationFluent setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public DbmsInputConfigurationFluent setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getDriverPath() {
        return driverPath;
    }

    public DbmsInputConfigurationFluent setDriverPath(String driverPath) {
        this.driverPath = driverPath;
        return this;
    }

    public Properties getConnectionProperties() {
        return connectionProperties;
    }

    public DbmsInputConfigurationFluent setConnectionProperties(Properties connectionProperties) {
        this.connectionProperties = connectionProperties;
        return this;
    }

    public Map<String, String> getConnectionProps() {
        return connectionProps;
    }

    public DbmsInputConfigurationFluent setConnectionProps(Map<String, String> connectionProps) {
        this.connectionProps = connectionProps;
        return this;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public DbmsInputConfigurationFluent setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public String getCatalog() {
        return catalog;
    }

    public DbmsInputConfigurationFluent setCatalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public DbmsInputConfigurationFluent setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getSchemaMetaPath() {
        return schemaMetaPath;
    }

    public DbmsInputConfigurationFluent setSchemaMetaPath(String schemaMetaPath) {
        this.schemaMetaPath = schemaMetaPath;
        return this;
    }

    public int getMaxDbThreads() {
        return maxDbThreads;
    }

    public DbmsInputConfigurationFluent setMaxDbThreads(Integer maxDbThreads) {
        this.maxDbThreads = maxDbThreads;
        return this;
    }

    @Override
    public boolean isViewsEnabled() {
        return !noViews;
    }

    public DbmsInputConfigurationFluent setNoViews(boolean noViews) {
        this.noViews = noViews;
        return this;
    }

    @Override
    public boolean isNumRowsEnabled() {
        return !noRows;
    }

    public DbmsInputConfigurationFluent setNoRows(boolean noRows) {
        this.noRows = noRows;
        return this;
    }

    public Pattern getTableInclusions() {
        return tableInclusions;
    }

    public DbmsInputConfigurationFluent setTableInclusions(Pattern tableInclusions) {
        this.tableInclusions = tableInclusions;
        return this;
    }

    public Pattern getTableExclusions() {
        return tableExclusions;
    }

    public DbmsInputConfigurationFluent setTableExclusions(Pattern tableExclusions) {
        this.tableExclusions = tableExclusions;
        return this;
    }

    public Pattern getColumnExclusions() {
        return columnExclusions;
    }

    public DbmsInputConfigurationFluent setColumnExclusions(Pattern columnExclusions) {
        this.columnExclusions = columnExclusions;
        return this;
    }

    public Pattern getIndirectColumnExclusions() {
        return indirectColumnExclusions;
    }

    public DbmsInputConfigurationFluent setIndirectColumnExclusions(Pattern indirectColumnExclusions) {
        this.indirectColumnExclusions = indirectColumnExclusions;
        return this;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public DbmsInputConfigurationFluent setArguments(Map<String, String> arguments) {
        this.arguments.putAll(arguments);
        return this;
    }

    public DbmsInputConfigurationFluent addArgument(String name, String value) {
        this.arguments.put(name,value);
        return this;
    }
}
