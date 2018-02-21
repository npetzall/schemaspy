package org.schemaspy.service;

import org.schemaspy.Config;
import org.schemaspy.api.progress.ProgressListener;
import org.schemaspy.api.progress.ProgressListenerFactory;
import org.schemaspy.model.*;
import org.schemaspy.model.xml.SchemaMeta;
import org.schemaspy.model.xml.TableMeta;
import org.schemaspy.service.helper.BasicTableMeta;
import org.schemaspy.validator.NameValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by rkasa on 2016-12-10.
 */
public class DatabaseService {

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TableService tableService;

    private final ViewService viewService;

    private final SqlService sqlService;

    private final ProgressListenerFactory progressListenerFactory;

    public DatabaseService(TableService tableService, ViewService viewService, SqlService sqlService, ProgressListenerFactory progressListenerFactory) {
        this.tableService = Objects.requireNonNull(tableService);
        this.viewService = Objects.requireNonNull(viewService);
        this.sqlService = Objects.requireNonNull(sqlService);
        this.progressListenerFactory = Objects.requireNonNull(progressListenerFactory);
    }

    public void gatheringSchemaDetails(Config config, Database db) throws SQLException {
        ProgressListener gatheringSchemaDetailsProgressListener = progressListenerFactory.newProgressListener("Gathering schema details");
        int tasks = config.isViewsEnabled() ? 15 : 14;
        gatheringSchemaDetailsProgressListener.starting(tasks);
        try {
            DatabaseMetaData meta = sqlService.getMeta();

            initTables(config, db, meta);
            gatheringSchemaDetailsProgressListener.finishedTask();
            if (config.isViewsEnabled()) {
                initViews(config, db, meta);
                gatheringSchemaDetailsProgressListener.finishedTask();
            }
            initCatalogs(db);
            gatheringSchemaDetailsProgressListener.finishedTask();
            initSchemas(db);
            gatheringSchemaDetailsProgressListener.finishedTask();
            initCheckConstraints(config, db);
            gatheringSchemaDetailsProgressListener.finishedTask();
            initTableIds(config, db);
            gatheringSchemaDetailsProgressListener.finishedTask();
            initIndexIds(config, db);
            gatheringSchemaDetailsProgressListener.finishedTask();
            initTableComments(config, db);
            gatheringSchemaDetailsProgressListener.finishedTask();
            initTableColumnComments(config, db);
            gatheringSchemaDetailsProgressListener.finishedTask();
            initViewComments(config, db);
            gatheringSchemaDetailsProgressListener.finishedTask();
            initViewColumnComments(config, db);
            gatheringSchemaDetailsProgressListener.finishedTask();
            initColumnTypes(config, db);
            gatheringSchemaDetailsProgressListener.finishedTask();
            initRoutines(config, db);
            gatheringSchemaDetailsProgressListener.finishedTask();

            connectTables(db);
            gatheringSchemaDetailsProgressListener.finishedTask();
            updateFromXmlMetadata(config, db, db.getSchemaMeta());
            gatheringSchemaDetailsProgressListener.finishedTask();
        } finally {
            gatheringSchemaDetailsProgressListener.finished();

        }
    }
    
   private void initCatalogs(Database db) throws SQLException {

            String sql = Config.getInstance().getDbProperties().getProperty("selectCatalogsSql");

            if (sql != null && db.getCatalog() != null) {
                try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                         db.getCatalog().setComment(rs.getString("catalog_comment"));
                    }
                } catch (SQLException sqlException) {
                    LOGGER.error(sql);
                    throw sqlException;
                }
            }
    }

    private void initSchemas(Database db) throws SQLException {
    	  String sql = Config.getInstance().getDbProperties().getProperty("selectSchemasSql");

          if (sql != null &&  db.getSchema() != null) {
              try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                   ResultSet rs = stmt.executeQuery()) {

                  if (rs.next()) {
                       db.getSchema().setComment(rs.getString("schema_comment"));
                  }
              } catch (SQLException sqlException) {
                  LOGGER.error(sql);
                  throw sqlException;
              }
          }
    }

    /**
     * Create/initialize any tables in the schema.

     * @param metadata
     * @throws SQLException
     */
    private void initTables(Config config, Database db, final DatabaseMetaData metadata) throws SQLException {
        final Pattern include = config.getTableInclusions();
        final Pattern exclude = config.getTableExclusions();
        final int maxThreads = config.getMaxDbThreads();

        String[] types = getTypes(config, "tableTypes", "TABLE");
        NameValidator validator = new NameValidator("table", include, exclude, types);
        List<BasicTableMeta> entries = getBasicTableMeta(config, db, metadata, true, types)
                .stream()
                .filter(basicTableMeta ->
                        validator.isValid(basicTableMeta.getName(), basicTableMeta.getType())
                )
                .collect(Collectors.toList());

        ExecutorService executorService = Executors.newFixedThreadPool(config.getMaxDbThreads());
        ExecutorCompletionService<Table> executorCompletionService = new ExecutorCompletionService<>(executorService);
        ProgressListener initTablesProgressListener = progressListenerFactory.newProgressListener("Initializing tables").starting(entries.size());
        try {
            entries.forEach(basicTableMeta -> {
                CallableTableCreator callableTableCreator = new CallableTableCreator(db, basicTableMeta, tableService, config.isNumRowsEnabled(), progressListenerFactory.newProgressListener("Creating model for " + basicTableMeta.getName()));
                executorCompletionService.submit(callableTableCreator);
            });
            for (int i = 0; i < entries.size(); i++) {
                Future<Table> futureTable = executorCompletionService.take();
                try {
                    Table table = futureTable.get();
                    db.getTablesMap().put(table.getName(), table);
                    initTablesProgressListener.finishedTask();
                } catch (ExecutionException e) {
                    LOGGER.warn("Failed to create model", e);
                    initTablesProgressListener.finishedTask();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            initTablesProgressListener.finished();
            executorService.shutdown();
        }
    }

    /**
     * Create/initialize any views in the schema.
     *
     * @param metadata
     * @throws SQLException
     */
    private void initViews(Config config, Database db, DatabaseMetaData metadata) throws SQLException {
        Pattern includeTables = config.getTableInclusions();
        Pattern excludeTables = config.getTableExclusions();

        String[] types = getTypes(config, "viewTypes", "VIEW");
        NameValidator validator = new NameValidator("view", includeTables, excludeTables, types);
        List<BasicTableMeta> entries = getBasicTableMeta(config, db, metadata, false, types)
                .stream()
                .filter(basicTableMeta -> validator.isValid(basicTableMeta.getName(), basicTableMeta.getType()))
                .collect(Collectors.toList());

        ProgressListener initViewProgressListener = progressListenerFactory.newProgressListener("Initializing views").starting(entries.size());
        try {
            for (BasicTableMeta entry : entries) {
                View view = new View(db, entry.getCatalog(), entry.getSchema(), entry.getName(),
                        entry.getRemarks(), entry.getViewDefinition());

                tableService.gatheringTableDetails(db, view);

                if (entry.getViewDefinition() == null) {
                    view.setViewDefinition(viewService.fetchViewDefinition(db, view));
                }

                db.getViewsMap().put(view.getName(), view);
                initViewProgressListener.finishedTask();
                LOGGER.debug("Found details of view {}", view.getName());
            }
        } finally {
            initViewProgressListener.finished();
        }
    }

    /**
     * Return a database-specific array of types from the .properties file
     * with the specified property name.
     *
     * @param propName
     * @param defaultValue
     * @return
     */
    private String[] getTypes(Config config, String propName, String defaultValue) {
        String value = config.getDbProperties().getProperty(propName, defaultValue);
        List<String> types = new ArrayList<>();
        for (String type : value.split(",")) {
            type = type.trim();
            if (type.length() > 0)
                types.add(type);
        }

        return types.toArray(new String[types.size()]);
    }

    /**
     * Take the supplied XML-based metadata and update our model of the schema with it
     *
     * @param schemaMeta
     * @throws SQLException
     */
    private void updateFromXmlMetadata(Config config, Database db, SchemaMeta schemaMeta) throws SQLException {
        if (schemaMeta != null) {
            if (Objects.nonNull(schemaMeta.getComments())) {
                config.setDescription(schemaMeta.getComments());
            }

            // done in three passes:
            // 1: create any new tables
            // 2: add/mod columns
            // 3: connect

            // add the newly defined tables and columns first
            for (TableMeta tableMeta : schemaMeta.getTables()) {
                Table table;

                if (tableMeta.getRemoteSchema() != null || tableMeta.getRemoteCatalog() != null) {
                    // will add it if it doesn't already exist
                    table = tableService.addRemoteTable(db, tableMeta.getRemoteCatalog(), tableMeta.getRemoteSchema(), tableMeta.getName(), db.getSchema().getName(), true);
                } else {
                    table = db.getLocals().get(tableMeta.getName());

                    if (table == null) {
                        // new table defined only in XML metadata
                        table = new LogicalTable(db, db.getCatalog().getName(), db.getSchema().getName(), tableMeta.getName(), tableMeta.getComments());
                        db.getTablesMap().put(table.getName(), table);
                    }
                }

                table.update(tableMeta);
            }

            // then tie the tables together
            for (TableMeta tableMeta : schemaMeta.getTables()) {
                Table table;

                if (tableMeta.getRemoteCatalog() != null || tableMeta.getRemoteSchema() != null) {
                    table = db.getRemoteTablesMap().get(db.getRemoteTableKey(tableMeta.getRemoteCatalog(), tableMeta.getRemoteSchema(), tableMeta.getName()));
                } else {
                    table = db.getLocals().get(tableMeta.getName());
                }

                tableService.connect(db, table, tableMeta, db.getLocals());
            }
        }
    }

    private void connectTables(Database db) throws SQLException {
        ProgressListener connectTablesListener = progressListenerFactory.newProgressListener("Connecting tables").starting(db.getTables().size());
        try {
            for (Table table : db.getTables()) {
                tableService.connectForeignKeys(db, table, db.getLocals());
                connectTablesListener.finishedTask();
            }
        } finally {
            connectTablesListener.finished();
        }
        ProgressListener connectViewsListener = progressListenerFactory.newProgressListener("Connecting views").starting(db.getViews().size());
        try {
            for (Table view : db.getViews()) {
                tableService.connectForeignKeys(db, view, db.getLocals());
                connectViewsListener.finishedTask();
            }
        } finally {
            connectViewsListener.finished();
        }
    }

    /**
     * Return a list of basic details of the tables in the schema.
     *
     * @param metadata
     * @param forTables true if we're getting table data, false if getting view data
     * @return
     * @throws SQLException
     */
    private List<BasicTableMeta> getBasicTableMeta(Config config,
                                                   Database db,
                                                   DatabaseMetaData metadata,
                                                   boolean forTables,
                                                   String... types) throws SQLException {
        String queryName = forTables ? "selectTablesSql" : "selectViewsSql";
        String sql = config.getDbProperties().getProperty(queryName);
        List<BasicTableMeta> basics = new ArrayList<>();

        ProgressListener basicMetaProgressListener = progressListenerFactory.newProgressListener("Fetching basic information for " + (forTables ? "tables": "views")).starting(1);
        try {
            if (sql != null) {
                String clazz = forTables ? "table" : "view";


                try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        String name = rs.getString(clazz + "_name");
                        String cat = getOptionalString(rs, clazz + "_catalog");
                        String sch = getOptionalString(rs, clazz + "_schema");
                        if (cat == null && sch == null)
                            sch = db.getSchema().getName();
                        String remarks = getOptionalString(rs, clazz + "_comment");
                        String viewDefinition = forTables ? null : getOptionalString(rs, "view_definition");
                        String rows = forTables ? getOptionalString(rs, "table_rows") : null;
                        long numRows = rows == null ? -1 : Long.parseLong(rows);

                        basics.add(new BasicTableMeta(cat, sch, name, clazz, remarks, viewDefinition, numRows));
                    }
                } catch (SQLException sqlException) {
                    LOGGER.warn("Failed to retrieve '{}' with SQL: '{}'", clazz, sql, sqlException);
                }
            }

            if (basics.isEmpty()) {
                String lastTableName = null;
                try (ResultSet rs = metadata.getTables(null, db.getSchema().getName(), "%", types)) {
                    while (rs.next()) {
                        String name = rs.getString("TABLE_NAME");
                        lastTableName = name;
                        String type = rs.getString("TABLE_TYPE");
                        String cat = rs.getString("TABLE_CAT");
                        String schem = rs.getString("TABLE_SCHEM");
                        String remarks = getOptionalString(rs, "REMARKS");

                        basics.add(new BasicTableMeta(cat, schem, name, type, remarks, null, -1));
                    }
                } catch (SQLException exc) {
                    if (forTables)
                        throw exc;
                    LOGGER.warn("Ignoring view '{}' due to exception, will continue", lastTableName, exc);
                }
            }
        }finally {
            basicMetaProgressListener.finished();
        }

        return basics;
    }

    /**
     * Some databases don't play nice with their metadata.
     * E.g. Oracle doesn't have a REMARKS column at all.
     * This method ignores those types of failures, replacing them with null.
     */
    private String getOptionalString(ResultSet rs, String columnName)
    {
        try {
            return rs.getString(columnName);
        } catch (SQLException ignore) {
            return null;
        }
    }

    private void initCheckConstraints(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectCheckConstraintsSql");
        if (sql != null) {
            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db,null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = db.getLocals().get(tableName);
                    if (table != null)
                        table.addCheckConstraint(rs.getString("constraint_name"), rs.getString("text"));
                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve check constraints with SQL: '{}'", sql, sqlException);
            }
        }
    }

    private void initColumnTypes(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectColumnTypesSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = db.getLocals().get(tableName);
                    if (table != null) {
                        String columnName = rs.getString("column_name");
                        TableColumn column = table.getColumn(columnName);
                        if (column != null) {
                            column.setTypeName(rs.getString("column_type"));
                            column.setShortType(getOptionalString(rs, "short_column_type"));
                        }
                    }
                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve column type details with SQL: '{}'", sql, sqlException);
            }
        }
    }

    private void initTableIds(Config config, Database db) throws SQLException {
        String sql = config.getDbProperties().getProperty("selectTableIdsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = db.getLocals().get(tableName);
                    if (table != null)
                        table.setId(rs.getObject("table_id"));
                }
            } catch (SQLException sqlException) {
                LOGGER.error("Failed to initialize table ids with SQL: {}", sql, sqlException);
                throw sqlException;
            }
        }
    }

    private void initIndexIds(Config config, Database db) throws SQLException {
        String sql = config.getDbProperties().getProperty("selectIndexIdsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = db.getLocals().get(tableName);
                    if (table != null) {
                        TableIndex index = table.getIndex(rs.getString("index_name"));
                        if (index != null)
                            index.setId(rs.getObject("index_id"));
                    }
                }
            } catch (SQLException sqlException) {
                LOGGER.error("Failed to initialize index ids with SQL: {}", sql, sqlException);
                throw sqlException;
            }
        }
    }

    /**
     * Initializes table comments.
     * If the SQL also returns view comments then they're plugged into the
     * appropriate views.
     *
     * @throws SQLException
     */
    private void initTableComments(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectTableCommentsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = db.getLocals().get(tableName);
                    if (table != null)
                        table.setComments(rs.getString("comments"));
                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve table comments with SQL: '{}'", sql, sqlException);
            }
        }
    }

    /**
     * Initializes view comments.
     *
     * @throws SQLException
     */
    private void initViewComments(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectViewCommentsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String viewName = rs.getString("view_name");
                    if (viewName == null)
                        viewName = rs.getString("table_name");
                    Table view = db.getViewsMap().get(viewName);

                    if (view != null)
                        view.setComments(rs.getString("comments"));
                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve view comments with SQL: '{}'", sql, sqlException);
            }
        }
    }

    /**
     * Initializes table column comments.
     * If the SQL also returns view column comments then they're plugged into the
     * appropriate views.
     *
     * @throws SQLException
     */
    private void initTableColumnComments(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectColumnCommentsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = db.getLocals().get(tableName);
                    if (table != null) {
                        TableColumn column = table.getColumn(rs.getString("column_name"));
                        if (column != null)
                            column.setComments(rs.getString("comments"));
                    }
                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve column comments with SQL: '{}'", sql, sqlException);
            }
        }
    }

    /**
     * Initializes view column comments.
     *
     * @throws SQLException
     */
    private void initViewColumnComments(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectViewColumnCommentsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String viewName = rs.getString("view_name");
                    if (viewName == null)
                        viewName = rs.getString("table_name");
                    Table view = db.getViewsMap().get(viewName);

                    if (view != null) {
                        TableColumn column = view.getColumn(rs.getString("column_name"));
                        if (column != null)
                            column.setComments(rs.getString("comments"));
                    }
                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve view column comments with SQL: '{}'", sql, sqlException);
            }
        }
    }

    /**
     * Initializes stored procedures / functions.
     *
     * @throws SQLException
     */
    private void initRoutines(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectRoutinesSql");

        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String routineName = rs.getString("routine_name");
                    String routineType = rs.getString("routine_type");
                    String returnType = rs.getString("dtd_identifier");
                    String definitionLanguage = rs.getString("routine_body");
                    String definition = rs.getString("routine_definition");
                    String dataAccess = rs.getString("sql_data_access");
                    String securityType = rs.getString("security_type");
                    boolean deterministic = rs.getBoolean("is_deterministic");
                    String comment = getOptionalString(rs, "routine_comment");

                    Routine routine = new Routine(routineName, routineType,
                            returnType, definitionLanguage, definition,
                            deterministic, dataAccess, securityType, comment);
                    db.getRoutinesMap().put(routineName, routine);
                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve stored procedure/function details with SQL: '{}'", sql, sqlException);
            }
        }

        sql = config.getDbProperties().getProperty("selectRoutineParametersSql");

        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String routineName = rs.getString("specific_name");

                    Routine routine = db.getRoutinesMap().get(routineName);
                    if (routine != null) {
                        String paramName = rs.getString("parameter_name");
                        String type = rs.getString("dtd_identifier");
                        String mode = rs.getString("parameter_mode");

                        RoutineParameter param = new RoutineParameter(paramName, type, mode);
                        routine.addParameter(param);
                    }

                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve stored procedure/function details with SQL: '{}'", sql, sqlException);
            }
        }
    }
}
