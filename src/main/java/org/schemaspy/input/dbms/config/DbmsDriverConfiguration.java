package org.schemaspy.input.dbms.config;

public class DbmsDriverConfiguration {

    private final String driverPath;
    private final String driverClass;

    public DbmsDriverConfiguration(String driverPath, String driverClass) {
        this.driverPath = driverPath;
        this.driverClass = driverClass;
    }

    public String getDriverPath() {
        return driverPath;
    }

    public String getDriverClass() {
        return driverClass;
    }
}
