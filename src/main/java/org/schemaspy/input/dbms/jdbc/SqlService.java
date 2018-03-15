/*
 * Copyright (C) 2016, 2017 Rafal Kasa
 * Copyright (C) 2017 Nils Petzaell
 */

package org.schemaspy.input.dbms.jdbc;

import org.schemaspy.model.Database;
import org.schemaspy.model.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rkasa on 2016-12-10.
 *
 * @author Rafal Kasa
 * @author Nils Petzaell
 */
public class SqlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Connection connection;
    private DatabaseMetaData databaseMetaData;
    private final Pattern invalidIdentifierPattern;
    private final Set<String> sqlKeywords;

    public SqlService(
            Connection connection,
            DatabaseMetaData databaseMetaData,
            Pattern invalidIdentifierPattern,
            Set<String> sqlKeywords) {
        this.connection = connection;
        this.databaseMetaData = databaseMetaData;
        this.invalidIdentifierPattern = invalidIdentifierPattern;
        this.sqlKeywords = Collections.unmodifiableSet(sqlKeywords);
    }

    public DatabaseMetaData getDatabaseMetaData() {
        return databaseMetaData;
    }

    public String getDatabaseProductName() {
        try {
            return databaseMetaData.getDatabaseProductName() + " - " + databaseMetaData.getDatabaseProductVersion();
        } catch (SQLException exc) {
            return "";
        }
    }

    public Set<String> getSqlKeywords(){
        return sqlKeywords;
    }

    public String getQuotedIdentifier(String id) throws SQLException {
        // look for any character that isn't valid (then matcher.find() returns true)
        Matcher matcher = invalidIdentifierPattern.matcher(id);

        boolean quotesRequired = matcher.find() || sqlKeywords.contains(id);

        if (quotesRequired) {
            // name contains something that must be quoted
            return quoteIdentifier(id);
        }

        // no quoting necessary
        return id;
    }

    public String quoteIdentifier(String id) throws SQLException {
        String quote = databaseMetaData.getIdentifierQuoteString().trim();
        return quote + id + quote;
    }

    /**
     * Create a <code>PreparedStatement</code> from the specified SQL.
     * The SQL can contain these named parameters (but <b>not</b> question marks).
     * <ol>
     * <li>:schema - replaced with the name of the schema
     * <li>:owner - alias for :schema
     * <li>:table - replaced with the name of the table
     * </ol>
     *
     * @param sql       String - SQL without question marks
     * @param tableName String - <code>null</code> if the statement doesn't deal with <code>Table</code>-level details.
     * @return PreparedStatement
     * @throws SQLException
     */
    public PreparedStatement prepareStatement(String sql, Database db, String tableName) throws SQLException {
        StringBuilder sqlBuf = new StringBuilder(sql);
        List<String> sqlParams = getSqlParams(sqlBuf, db.getName(), db.getCatalog().getName(), db.getSchema().getName(), tableName); // modifies sqlBuf
        LOGGER.debug("{} {}", sqlBuf, sqlParams);

        PreparedStatement stmt = connection.prepareStatement(sqlBuf.toString());
        try {
            for (int i = 0; i < sqlParams.size(); ++i) {
                stmt.setString(i + 1, sqlParams.get(i));
            }
        } catch (SQLException exc) {
            stmt.close();
            throw exc;
        }

        return stmt;
    }

    /**
     * Replaces named parameters in <code>sql</code> with question marks and
     * returns appropriate matching values in the returned <code>List</code> of <code>String</code>s.
     *
     * @param sql       StringBuffer input SQL with named parameters, output named params are replaced with ?'s.
     * @param tableName String
     * @return List of Strings
     * @see #prepareStatement(String, Database, String)
     */
    private List<String> getSqlParams(StringBuilder sql, String dbName, String catalog, String schema, String tableName) {
        Map<String, String> namedParams = new HashMap<>();
        if (Objects.isNull(schema)) {
            schema = dbName; // some 'schema-less' db's treat the db name like a schema (unusual case)
        }
        
        namedParams.put(":dbname", dbName);
        namedParams.put(":schema", schema);
        namedParams.put(":owner", schema); // alias for :schema
        if (Objects.nonNull(tableName)) {
            namedParams.put(":table", tableName);
            namedParams.put(":view", tableName); // alias for :table
        }
        if (Objects.nonNull(catalog)) {
            namedParams.put(":catalog", catalog);
        }

        List<String> sqlParams = new ArrayList<>();
        int nextColon = sql.indexOf(":");
        while (nextColon != -1) {
            String paramName = new StringTokenizer(sql.substring(nextColon), " ,\"')").nextToken();
            String paramValue = namedParams.get(paramName);
            if (Objects.isNull(paramValue))
                throw new InvalidConfigurationException("Unexpected named parameter '" + paramName + "' found in SQL '" + sql + "'");
            sqlParams.add(paramValue);
            sql.replace(nextColon, nextColon + paramName.length(), "?"); // replace with a ?
            nextColon = sql.indexOf(":", nextColon);
        }

        return sqlParams;
    }

    public PreparedStatement prepareStatement(String sqlQuery) throws SQLException {
        return connection.prepareStatement(sqlQuery);
    }
}
