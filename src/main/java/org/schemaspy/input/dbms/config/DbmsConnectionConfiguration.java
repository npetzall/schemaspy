package org.schemaspy.input.dbms.config;

import java.util.Properties;

public class DbmsConnectionConfiguration {

    private final String connectionUrl;
    private final Properties connectionProperties;

    public DbmsConnectionConfiguration(String connectionUrl, Properties connectionProperties) {
        this.connectionUrl = connectionUrl;
        this.connectionProperties = connectionProperties;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public Properties getConnectionProperties() {
        return connectionProperties;
    }
}
