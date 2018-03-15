package org.schemaspy.input.dbms.config;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public interface DbmsInputConfiguration {

    String getDatabaseType();
    String getHost();
    String getPort();
    String getUser();
    String getPassword();
    String getDriverPath();
    Properties getConnectionProperties();

    String getDatabaseName();
    String getCatalog();
    String getSchema();
    String getSchemaMetaPath();

    int getMaxDbThreads();

    boolean isViewsEnabled();
    boolean isNumRowsEnabled();

    Pattern getTableInclusions();
    Pattern getTableExclusions();
    Pattern getColumnExclusions();
    Pattern getIndirectColumnExclusions();

    Map<String,String> getArguments();
}
