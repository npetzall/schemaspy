package org.schemaspy.input.dbms;

import org.schemaspy.input.dbms.config.DbmsConfiguration;
import org.schemaspy.input.dbms.jdbc.SqlService;
import org.schemaspy.model.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;

public class DatabaseFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public Database createDatabase(DbmsConfiguration dbmsConfiguration, SqlService sqlService) {
        String databaseProductName = sqlService.getDatabaseProductName();
        Set<String> sqlKeywords = sqlService.getSqlKeywords();
        Set<String> systemFunctions = getSystemFunctions(sqlService.getDatabaseMetaData());
        Set<String> numericFunctions = getNumericFunctions(sqlService.getDatabaseMetaData());
        Set<String> stringFunctions = getStringFunctions(sqlService.getDatabaseMetaData());
        Set<String> timeDateFunctions = getTimeDateFunctions(sqlService.getDatabaseMetaData());
        return new Database(
                databaseProductName,
                dbmsConfiguration.getDatabaseName(),
                dbmsConfiguration.getCatalog(),
                dbmsConfiguration.getSchema(),
                dbmsConfiguration.getSchemaMeta(),
                sqlKeywords,
                systemFunctions,
                numericFunctions,
                stringFunctions,
                timeDateFunctions
        );
    }

    private Set<String> getSystemFunctions(DatabaseMetaData databaseMetaData) {
        String systemFunctionsCSV = null;
        try {
            systemFunctionsCSV = databaseMetaData.getSystemFunctions();
        } catch (SQLException e) {
            LOGGER.warn("Unable to fetch SystemFunctions", e);
        }
        return csvToSet(systemFunctionsCSV);
    }

    private Set<String> getNumericFunctions(DatabaseMetaData databaseMetaData) {
        String numericFunctionsCSV = null;
        try {
            numericFunctionsCSV = databaseMetaData.getNumericFunctions();
        } catch (SQLException e) {
            LOGGER.warn("Unable to fetch NumericFunctions", e);
        }
        return csvToSet(numericFunctionsCSV);
    }

    private Set<String> getStringFunctions(DatabaseMetaData databaseMetaData) {
        String stringFunctionsCSV = null;
        try {
            stringFunctionsCSV = databaseMetaData.getStringFunctions();
        } catch (SQLException e) {
            LOGGER.warn("Unable to fetch StringFunctions", e);
        }
        return csvToSet(stringFunctionsCSV);
    }

    private Set<String> getTimeDateFunctions(DatabaseMetaData databaseMetaData) {
        String timeDateFunctionsCSV = null;
        try {
            timeDateFunctionsCSV = databaseMetaData.getTimeDateFunctions();
        } catch (SQLException e) {
            LOGGER.warn("Unable to fetch TimeDateFunctions", e);
        }
        return csvToSet(timeDateFunctionsCSV);
    }

    private Set<String> csvToSet(String csv) {
        if (Objects.isNull(csv) || csv.trim().isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> set = new HashSet<>();
        try (Scanner scanner = new Scanner(csv.toUpperCase()).useDelimiter(",")) {
            while(scanner.hasNext()){
                set.add(scanner.next());
            }
        }
        return set;
    }
}
