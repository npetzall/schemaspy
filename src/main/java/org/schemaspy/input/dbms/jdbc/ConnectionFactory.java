package org.schemaspy.input.dbms.jdbc;

import org.schemaspy.input.dbms.config.DbmsConnectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;

public class ConnectionFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final DbmsConnectionConfiguration dbmsConnectionConfiguration;
    private final Driver driver;

    public ConnectionFactory(DbmsConnectionConfiguration dbmsConnectionConfiguration, Driver driver) {
        this.dbmsConnectionConfiguration = dbmsConnectionConfiguration;
        this.driver = driver;
    }

    public Connection getConnection() throws SQLException {
        return driver.connect(dbmsConnectionConfiguration.getConnectionUrl(), dbmsConnectionConfiguration.getConnectionProperties());
    }
}
