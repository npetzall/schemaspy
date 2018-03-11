package org.schemaspy.input.dbms.config;

import java.util.regex.Pattern;

public interface DbmsInputConfiguration {

    boolean isViewsEnabled();

    Pattern getTableInclusions();

    Pattern getTableExclusions();

    int getMaxDbThreads();

    Pattern getIndirectColumnExclusions();

    Pattern getColumnExclusions();

    String getDatabaseName();

    String getCatalog();

    String getSchema();

    String getSchemaMetaPath();

    String getDatabaseType();

    String getDriverPath();
}
