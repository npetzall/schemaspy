/*
 * Copyright (C) 2016, 2017 Rafal Kasa
 * Copyright (C) 2017 Nils Petzaell
 */

package org.schemaspy.input.dbms.jdbc;

import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.InvalidConfigurationException;
import org.schemaspy.util.ConnectionURLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
    private DatabaseMetaData meta;
    private Pattern invalidIdentifierPattern;

    public DatabaseMetaData getDatabaseMetaData() {
        return meta;
    }

    public DatabaseMetaData connect(Config config) throws IOException, SQLException {
        Properties properties = config.getDbProperties();

        ConnectionURLBuilder urlBuilder = new ConnectionURLBuilder(config, properties);
        if (Objects.isNull(config.getDb()))
            config.setDb(urlBuilder.build());

        String driverClass = properties.getProperty("driver");
        String driverPath = properties.getProperty("driverPath");
        if (Objects.isNull(driverPath))
            driverPath = "";

        if (Objects.nonNull(config.getDriverPath()))
            driverPath = config.getDriverPath();

        DbDriverLoader driverLoader = new DbDriverLoader();
        connection = driverLoader.getConnection(config, urlBuilder.build(), driverClass, driverPath);

        meta = connection.getMetaData();

        if (config.isEvaluateAllEnabled()) {
            return null;    // no database to return
        }

        return meta;
    }

    public String getDatabaseProductName() {
        try {
            return meta.getDatabaseProductName() + " - " + meta.getDatabaseProductVersion();
        } catch (SQLException exc) {
            return "";
        }
    }

    private Set<String> getSqlKeywords() {
        // from http://www.contrib.andrew.cmu.edu/~shadow/sql/sql1992.txt:
        String[] sql92Keywords =
                ("ADA" +
                        "| C | CATALOG_NAME | CHARACTER_SET_CATALOG | CHARACTER_SET_NAME" +
                        "| CHARACTER_SET_SCHEMA | CLASS_ORIGIN | COBOL | COLLATION_CATALOG" +
                        "| COLLATION_NAME | COLLATION_SCHEMA | COLUMN_NAME | COMMAND_FUNCTION | COMMITTED" +
                        "| CONDITION_NUMBER | CONNECTION_NAME | CONSTRAINT_CATALOG | CONSTRAINT_NAME" +
                        "| CONSTRAINT_SCHEMA | CURSOR_NAME" +
                        "| DATA | DATETIME_INTERVAL_CODE | DATETIME_INTERVAL_PRECISION | DYNAMIC_FUNCTION" +
                        "| FORTRAN" +
                        "| LENGTH" +
                        "| MESSAGE_LENGTH | MESSAGE_OCTET_LENGTH | MESSAGE_TEXT | MORE | MUMPS" +
                        "| NAME | NULLABLE | NUMBER" +
                        "| PASCAL | PLI" +
                        "| REPEATABLE | RETURNED_LENGTH | RETURNED_OCTET_LENGTH | RETURNED_SQLSTATE" +
                        "| ROW_COUNT" +
                        "| SCALE | SCHEMA_NAME | SERIALIZABLE | SERVER_NAME | SUBCLASS_ORIGIN" +
                        "| TABLE_NAME | TYPE" +
                        "| UNCOMMITTED | UNNAMED" +
                        "| ABSOLUTE | ACTION | ADD | ALL | ALLOCATE | ALTER | AND" +
                        "| ANY | ARE | AS | ASC" +
                        "| ASSERTION | AT | AUTHORIZATION | AVG" +
                        "| BEGIN | BETWEEN | BIT | BIT_LENGTH | BOTH | BY" +
                        "| CASCADE | CASCADED | CASE | CAST | CATALOG | CHAR | CHARACTER | CHAR_LENGTH" +
                        "| CHARACTER_LENGTH | CHECK | CLOSE | COALESCE | COLLATE | COLLATION" +
                        "| COLUMN | COMMIT | CONNECT | CONNECTION | CONSTRAINT" +
                        "| CONSTRAINTS | CONTINUE" +
                        "| CONVERT | CORRESPONDING | COUNT | CREATE | CROSS | CURRENT" +
                        "| CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | CURRENT_USER | CURSOR" +
                        "| DATE | DAY | DEALLOCATE | DEC | DECIMAL | DECLARE | DEFAULT | DEFERRABLE" +
                        "| DEFERRED | DELETE | DESC | DESCRIBE | DESCRIPTOR | DIAGNOSTICS" +
                        "| DISCONNECT | DISTINCT | DOMAIN | DOUBLE | DROP" +
                        "| ELSE | END | END-EXEC | ESCAPE | EXCEPT | EXCEPTION" +
                        "| EXEC | EXECUTE | EXISTS" +
                        "| EXTERNAL | EXTRACT" +
                        "| FALSE | FETCH | FIRST | FLOAT | FOR | FOREIGN | FOUND | FROM | FULL" +
                        "| GET | GLOBAL | GO | GOTO | GRANT | GROUP" +
                        "| HAVING | HOUR" +
                        "| IDENTITY | IMMEDIATE | IN | INDICATOR | INITIALLY | INNER | INPUT" +
                        "| INSENSITIVE | INSERT | INT | INTEGER | INTERSECT | INTERVAL | INTO | IS" +
                        "| ISOLATION" +
                        "| JOIN" +
                        "| KEY" +
                        "| LANGUAGE | LAST | LEADING | LEFT | LEVEL | LIKE | LOCAL | LOWER" +
                        "| MATCH | MAX | MIN | MINUTE | MODULE | MONTH" +
                        "| NAMES | NATIONAL | NATURAL | NCHAR | NEXT | NO | NOT | NULL" +
                        "| NULLIF | NUMERIC" +
                        "| OCTET_LENGTH | OF | ON | ONLY | OPEN | OPTION | OR" +
                        "| ORDER | OUTER" +
                        "| OUTPUT | OVERLAPS" +
                        "| PAD | PARTIAL | POSITION | PRECISION | PREPARE | PRESERVE | PRIMARY" +
                        "| PRIOR | PRIVILEGES | PROCEDURE | PUBLIC" +
                        "| READ | REAL | REFERENCES | RELATIVE | RESTRICT | REVOKE | RIGHT" +
                        "| ROLLBACK | ROWS" +
                        "| SCHEMA | SCROLL | SECOND | SECTION | SELECT | SESSION | SESSION_USER | SET" +
                        "| SIZE | SMALLINT | SOME | SPACE | SQL | SQLCODE | SQLERROR | SQLSTATE" +
                        "| SUBSTRING | SUM | SYSTEM_USER" +
                        "| TABLE | TEMPORARY | THEN | TIME | TIMESTAMP | TIMEZONE_HOUR | TIMEZONE_MINUTE" +
                        "| TO | TRAILING | TRANSACTION | TRANSLATE | TRANSLATION | TRIM | TRUE" +
                        "| UNION | UNIQUE | UNKNOWN | UPDATE | UPPER | USAGE | USER | USING" +
                        "| VALUE | VALUES | VARCHAR | VARYING | VIEW" +
                        "| WHEN | WHENEVER | WHERE | WITH | WORK | WRITE" +
                        "| YEAR" +
                        "| ZONE").split("[| ]+");

        String[] nonSql92Keywords = new String[0];
        try {
            nonSql92Keywords = meta.getSQLKeywords().toUpperCase().split(",\\s*");
        } catch (SQLException sqle) {
            LOGGER.warn("Failed to retrieve SQLKeywords from metadata, using only SQL92 keywords");
            LOGGER.debug("Failed to retrieve SQLKeywords from metadata, using only SQL92 keywords", sqle);
        }

        Set<String> sqlKeywords = new HashSet<String>() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean contains(Object key) {
                return super.contains(((String)key).toUpperCase());
            }
        };
        sqlKeywords.addAll(Arrays.asList(sql92Keywords));
        sqlKeywords.addAll(Arrays.asList(nonSql92Keywords));

        return sqlKeywords;
    }

    public String getQuotedIdentifier(String id) throws SQLException {
        // look for any character that isn't valid (then matcher.find() returns true)
        Matcher matcher = getInvalidIdentifierPattern().matcher(id);

        boolean quotesRequired = matcher.find() || getSqlKeywords().contains(id);

        if (quotesRequired) {
            // name contains something that must be quoted
            return quoteIdentifier(id);
        }

        // no quoting necessary
        return id;
    }

    public String quoteIdentifier(String id) throws SQLException {
        String quote = meta.getIdentifierQuoteString().trim();
        return quote + id + quote;
    }

    /**
     * Return a <code>Pattern</code> whose matcher will return <code>true</code>
     * when run against an identifier that contains a character that is not
     * acceptable by the database without being quoted.
     */
    private Pattern getInvalidIdentifierPattern() throws SQLException {
        if (invalidIdentifierPattern == null) {
            StringBuilder validChars = new StringBuilder("a-zA-Z0-9_");
            String reservedRegexChars = "-&^";
            String extraValidChars = meta.getExtraNameCharacters();
            for (int i = 0; i < extraValidChars.length(); ++i) {
                char ch = extraValidChars.charAt(i);
                if (reservedRegexChars.indexOf(ch) >= 0)
                    validChars.append("" + "\\");
                validChars.append(ch);
            }

            invalidIdentifierPattern = Pattern.compile("[^" + validChars + "]");
        }

        return invalidIdentifierPattern;
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
