package org.schemaspy.input.dbms.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class SqlServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public SqlService createSqlService(Connection connection) throws SQLException {
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        LOGGER.info("Connected to {} - {}", databaseMetaData.getDatabaseProductName(), databaseMetaData.getDatabaseProductVersion());
        LOGGER.debug("supportsSchemasInTableDefinitions: {}", databaseMetaData.supportsSchemasInTableDefinitions());
        LOGGER.debug("supportsCatalogsInTableDefinitions: {}", databaseMetaData.supportsCatalogsInTableDefinitions());
        return new SqlService(connection, databaseMetaData, getInvalidIdentifierPattern(databaseMetaData), getSqlKeywords(databaseMetaData));
    }

    private Set<String> getSqlKeywords(DatabaseMetaData databaseMetaData) {
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
            nonSql92Keywords = databaseMetaData.getSQLKeywords().toUpperCase().split(",\\s*");
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

    /**
     * Return a <code>Pattern</code> whose matcher will return <code>true</code>
     * when run against an identifier that contains a character that is not
     * acceptable by the database without being quoted.
     */
    private Pattern getInvalidIdentifierPattern(DatabaseMetaData databaseMetaData) throws SQLException {
        StringBuilder validChars = new StringBuilder("a-zA-Z0-9_");
        String reservedRegexChars = "-&^";
        String extraValidChars = databaseMetaData.getExtraNameCharacters();
        for (int i = 0; i < extraValidChars.length(); ++i) {
            char ch = extraValidChars.charAt(i);
            if (reservedRegexChars.indexOf(ch) >= 0)
                validChars.append("" + "\\");
            validChars.append(ch);
        }
        return Pattern.compile("[^" + validChars + "]");
    }
}
