package org.schemaspy.cli;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.schemaspy.cli.converters.PatternConverter;
import org.schemaspy.cli.converters.PropertiesConverter;
import org.schemaspy.input.dbms.config.DbmsInputConfiguration;

import java.util.*;
import java.util.regex.Pattern;

@Parameters(resourceBundle = "dbmscommandlinearguments")
public class DbmsCommandLineArguments implements DbmsInputConfiguration{

    @Parameter(
            names = {
                    "-t", "--database-type", "database-type",
                    "schemaspy.t", "schemaspy.database-type"
            },
            descriptionKey = "databaseType"
    )
    private String databaseType;
    @Parameter(
            names = {
                    "-host", "--host", "host",
                    "schamspy.host"
            },
            descriptionKey = "host"
    )
    private String host;
    @Parameter(
            names = {
                    "-port", "--port", "port",
                    "schemaspy.port"
            },
            descriptionKey = "port"
    )
    private Integer port;
    @Parameter(
            names = {
                    "-u", "--user", "user",
                    "schemaspy.u", "schemaspy.user"},
            descriptionKey = "user"
    )
    private String user;
    @Parameter(
            names = {
                    "-p", "--password", "password",
                    "schemaspy.p", "schemaspy.password"
            },
            descriptionKey = "password"//,
            //password = true
    )
    private String password;
    @Parameter(
            names = {
                    "-dp", "--driver-path", "driverPath",
                    "schemaspy.dp", "schemaspy.driverPath"
            },
            descriptionKey = "driverPath"
    )
    private String driverPath;
    @Parameter(
            names = {
                    "-connprops", "--connection-properties-file", "connprops",
                    "schemaspy.connprops", "schemaspy.connectionProperties"
            },
            descriptionKey = "connProps",
            converter = PropertiesConverter.class
    )
    private Properties connectionProperties = new Properties();
    @DynamicParameter(
            names = {
                    "-connProp", "--connection-property", "connProp",
                    "schemaspy.connProp"
            },
            descriptionKey = "connProp"

    )
    private Map<String,String> connectionProps = new HashMap<>();

    @Parameter(
            names = {
                    "-db", "--database-name",
                    "schemaspy.db", "schemaspy.database-name"
            },
            descriptionKey = "databaseName"
    )
    private String databaseName;
    @Parameter(
            names = {
                    "-cat", "--catalog", "catalog",
                    "schemaspy.cat", "schemaspy.catalog"
            },
            descriptionKey = "catalog"
    )
    private String catalog;
    @Parameter(
            names = {
                    "-s", "--schema", "schema",
                    "schemaspy.s", "schemaspy.schema"

            },
            descriptionKey = "schema"
    )
    private String schema;
    @Parameter(
            names = {
                    "-meta", "--schema-meta", "meta",
                    "schemaspy.meta", "schemaspy.schema-meta"
            },
            descriptionKey = "schemaMeta"
    )
    private String schemaMetaPath;

    @Parameter(
            names = {
                    "-dbThreads", "-dbthreads", "--db-threads", "dbThreads",
                    "schemaspy.dbThreads"
            },
            descriptionKey = "maxDbThreads"
    )
    private Integer maxDbThreads;
    @Parameter(
            names = {
                    "-noviews", "--no-views", "noviews",
                    "schemaspy.noviews"
            },
            descriptionKey = "noViews"
    )
    private boolean noViews = false;
    @Parameter(
            names = {
                    "-norows", "--no-rows", "norows",
                    "schemaspy.norows"
            },
            descriptionKey = "noRows"
    )
    private boolean noRows = false;

    @Parameter(
            names = {
                    "-i", "--table-inclusion", "i",
                    "schemaspy.tableInclusions"
            },
            descriptionKey = "tableInclusions",
            converter = PatternConverter.class
    )
    private Pattern tableInclusions = Pattern.compile(".*");
    @Parameter(
            names = {
                    "-I", "--table-exlusions", "I",
                    "schemaspy.tableExclusions"
            },
            descriptionKey = "tableExclusions",
            converter = PatternConverter.class
    )
    private Pattern tableExclusions = Pattern.compile("");
    @Parameter(
            names = {
                    "-X", "--column-exclusions", "X",
                    "schemaspy.columnExclusions"
            },
            descriptionKey = "columnExclusions",
            converter = PatternConverter.class
    )
    private Pattern columnExclusions = Pattern.compile("[^.]");
    @Parameter(
            names = {
                    "-x", "--indirect-column-exclusions", "x",
                    "schemaspy.indirectColumnExclusions"
            },
            descriptionKey = "indirectColumnExclusions",
            converter = PatternConverter.class
    )
    private Pattern indirectColumnExclusions = Pattern.compile("[^.]");

    private Map<String, String> arguments = new HashMap<>();

    @Override
    public String getDatabaseType() {
        return databaseType;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getPort() {
        return String.valueOf(port.toString());
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getDriverPath() {
        return driverPath;
    }

    @Override
    public Properties getConnectionProperties() {
        Properties properties = new Properties(connectionProperties);
        connectionProps.forEach(properties::setProperty);
        return properties;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String getCatalog() {
        return catalog;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public String getSchemaMetaPath() {
        return schemaMetaPath;
    }

    @Override
    public int getMaxDbThreads() {
        return maxDbThreads;
    }

    @Override
    public boolean isViewsEnabled() {
        return !noViews;
    }

    @Override
    public boolean isNumRowsEnabled() {
        return !noRows;
    }

    @Override
    public Pattern getTableInclusions() {
        return tableInclusions;
    }

    @Override
    public Pattern getTableExclusions() {
        return tableExclusions;
    }

    @Override
    public Pattern getColumnExclusions() {
        return columnExclusions;
    }

    @Override
    public Pattern getIndirectColumnExclusions() {
        return indirectColumnExclusions;
    }

    public void setArguments(List<String> unknowns) {
        ListIterator<String> listIterator = unknowns.listIterator();
        while (listIterator.hasNext()) {
            String key = listIterator.next();
            String value = listIterator.hasNext() ? listIterator.next() : Boolean.TRUE.toString();
            if (key.startsWith("-")) {
                if (!value.startsWith("-")) {
                    arguments.put(key.substring(1), value);
                } else {
                    arguments.put(key.substring(1), Boolean.TRUE.toString());
                    listIterator.previous();
                }
            } else {
                listIterator.previous();
            }
        }
    }

    @Override
    public Map<String, String> getArguments() {
        return arguments;
    }
}
