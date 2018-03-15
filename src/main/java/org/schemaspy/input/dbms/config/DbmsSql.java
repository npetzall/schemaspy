package org.schemaspy.input.dbms.config;

import java.util.Properties;

public class DbmsSql {

    private final Properties properties;
    private final String[] tableTypes;
    private final String[] viewTypes;

    public DbmsSql(Properties properties, String[] tableTypes, String[] viewTypes) {
        this.properties = properties;
        this.tableTypes = tableTypes;
        this.viewTypes = viewTypes;
    }

    public String[] getTableTypes() {
        return tableTypes;
    }

    public String[] getViewTypes() {
        return viewTypes;
    }

    public String getSelectCatalogsSql() {
        return properties.getProperty("selectCatalogsSql");
    }

    public String getSelectSchemasSql() {
        return properties.getProperty("selectSchemasSql");
    }

    public String getSelectCheckConstraintsSql() {
        return properties.getProperty("selectCheckConstraintsSql");
    }

    public String getSelectColumnTypesSql() {
        return properties.getProperty("selectColumnTypesSql");
    }

    public String getSelectTableIdsSql() {
        return properties.getProperty("selectTableIdsSql");
    }

    public String getSelectIndexIdsSql() {
        return properties.getProperty("selectIndexIdsSql");
    }

    public String getSelectTableCommentsSql() {
        return properties.getProperty("selectTableCommentsSql");
    }

    public String getSelectViewCommentsSql() {
        return properties.getProperty("selectViewCommentsSql");
    }

    public String getSelectColumnCommentsSql() {
        return properties.getProperty("selectColumnCommentsSql");
    }

    public String getSelectViewColumnCommentsSql() {
        return properties.getProperty("selectViewColumnCommentsSql");
    }

    public String getSelectRoutinesSql() {
        return properties.getProperty("selectRoutinesSql");
    }

    public String getSelectRoutineParametersSql() {
        return properties.getProperty("selectRoutineParametersSql");
    }

    public String getSelectTablesSql() {
        return properties.getProperty("selectTablesSql");
    }

    public String getSelectViewsSql() {
        return properties.getProperty("selectViewsSql");
    }

    public String getSelectViewSql() {
        return properties.getProperty("selectViewSql");
    }

    public String getSelectRowCountSql() {
        return properties.getProperty("selectRowCountSql");
    }

    public String getSelectIndexesSql() {
        return properties.getProperty("selectIndexesSql");
    }
}
