package org.schemaspy.input.dbms.jdbc;

import org.schemaspy.input.dbms.NameValidator;
import org.schemaspy.input.dbms.config.DbmsConfiguration;
import org.schemaspy.input.dbms.config.DbmsSql;
import org.schemaspy.input.dbms.jdbc.helper.BasicTableMeta;
import org.schemaspy.model.*;
import org.schemaspy.model.xml.SchemaMeta;
import org.schemaspy.model.xml.TableMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by rkasa on 2016-12-10.
 */
public class DatabaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final DbmsConfiguration dbmsConfiguration;
    private final DbmsSql dbmsSql;

    private final TableService tableService;

    private final ViewService viewService;

    private final SqlService sqlService;


    public DatabaseService(DbmsConfiguration dbmsConfiguration, TableService tableService, ViewService viewService, SqlService sqlService) {
        this.dbmsConfiguration = Objects.requireNonNull(dbmsConfiguration);
        this.dbmsSql = dbmsConfiguration.getDbmsSql();
        this.tableService = Objects.requireNonNull(tableService);
        this.viewService = Objects.requireNonNull(viewService);
        this.sqlService = Objects.requireNonNull(sqlService);
    }

    public void gatheringSchemaDetails(Database db, ProgressListener listener) throws SQLException {
        LOGGER.info("Gathering schema details");

        listener.startedGatheringDetails();

        initTables(db, listener);
        if (dbmsConfiguration.isViewsEnabled())
            initViews(db, listener);
        
        initCatalogs(db);
        initSchemas(db);

        initCheckConstraints(db, listener);
        initTableIds(db);
        initIndexIds(db);
        initTableComments(db, listener);
        initTableColumnComments(db, listener);
        initViewComments(db, listener);
        initViewColumnComments(db, listener);
        initColumnTypes(db, listener);
        initRoutines(db, listener);

        listener.startedConnectingTables();

        connectTables(db, listener);
        updateFromXmlMetadata(db, db.getSchemaMeta());
    }
    
    private void initCatalogs(Database db) throws SQLException {

            String sql = dbmsSql.getSelectCatalogsSql();

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
    	  String sql = dbmsSql.getSelectSchemasSql();

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

     * @throws SQLException
     */
    private void initTables(Database db, ProgressListener listener) throws SQLException {
        final Pattern include = dbmsConfiguration.getTableInclusions();
        final Pattern exclude = dbmsConfiguration.getTableExclusions();
        final int maxThreads = dbmsConfiguration.getMaxDbThreads();

        String[] types = dbmsSql.getTableTypes();

        NameValidator validator = new NameValidator("table", include, exclude, types);
        List<BasicTableMeta> entries = getBasicTableMeta(db, listener, true, types);

        TableCreator creator;
        if (maxThreads == 1) {
            creator = new TableCreator();
        } else {
            // creating tables takes a LONG time (based on JProbe analysis),
            // so attempt to speed it up by doing several in parallel.
            // note that it's actually DatabaseMetaData.getIndexInfo() that's expensive

            creator = new ThreadedTableCreator(maxThreads);

            // "prime the pump" so if there's a database problem we'll probably see it now
            // and not in a secondary thread
            while (!entries.isEmpty()) {
                BasicTableMeta entry = entries.remove(0);

                if (validator.isValid(entry.getName(), entry.getType())) {
                    new TableCreator().create(db, entry, listener);
                    break;
                }
            }
        }

        // kick off the secondary threads to do the creation in parallel
        for (BasicTableMeta entry : entries) {
            if (validator.isValid(entry.getName(), entry.getType())) {
                creator.create(db, entry, listener);
            }
        }

        // wait for everyone to finish
        creator.join();
    }

    /**
     * Create/initialize any views in the schema.
     *
     * @throws SQLException
     */
    private void initViews(Database db, ProgressListener listener) throws SQLException {
        Pattern includeTables = dbmsConfiguration.getTableInclusions();
        Pattern excludeTables = dbmsConfiguration.getTableExclusions();

        String[] types = dbmsSql.getViewTypes();
        NameValidator validator = new NameValidator("view", includeTables, excludeTables, types);

        for (BasicTableMeta entry : getBasicTableMeta(db, listener, false, types)) {
            if (validator.isValid(entry.getName(), entry.getType())) {
                View view = new View(db, entry.getCatalog(), entry.getSchema(), entry.getName(),
                        entry.getRemarks(), entry.getViewDefinition());

                tableService.gatheringTableDetails(db, view);

                if (entry.getViewDefinition() == null) {
                    view.setViewDefinition(viewService.fetchViewDefinition(dbmsSql.getSelectViewSql(), db, view));
                }

                db.getViewsMap().put(view.getName(), view);
                listener.gatheringDetailsProgressed(view);

                LOGGER.debug("Found details of view {}", view.getName());
            }
        }
    }

    /**
     * Take the supplied XML-based metadata and update our model of the schema with it
     *
     * @param schemaMeta
     * @throws SQLException
     */
    private void updateFromXmlMetadata(Database db, SchemaMeta schemaMeta) throws SQLException {
        if (schemaMeta != null) {
            LOGGER.info("Using additional metadata from {}", schemaMeta.getFile());
            if (Objects.nonNull(schemaMeta.getComments())) {
                db.getSchema().setComment(schemaMeta.getComments());
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

    private void connectTables(Database db, ProgressListener listener) throws SQLException {
        for (Table table : db.getTables()) {
            listener.connectingTablesProgressed(table);

            tableService.connectForeignKeys(db, table, db.getLocals());
        }

        for (Table view : db.getViews()) {
            listener.connectingTablesProgressed(view);

            tableService.connectForeignKeys(db, view, db.getLocals());
        }
    }

    /**
     * Single-threaded implementation of a class that creates tables
     */
    private class TableCreator {
        /**
         * Create a table and put it into <code>tables</code>
         */
        void create(Database db, BasicTableMeta tableMeta, ProgressListener listener) throws SQLException {
            createImpl(db, tableMeta, listener);
        }

        protected void createImpl(Database db, BasicTableMeta tableMeta, ProgressListener listener) throws SQLException {
            Table table = new Table(db, tableMeta.getCatalog(), tableMeta.getSchema(), tableMeta.getName(), tableMeta.getRemarks());
            tableService.gatheringTableDetails(db, table);

            if (tableMeta.getNumRows() != -1) {
                table.setNumRows(tableMeta.getNumRows());
            }

            if (table.getNumRows() == 0) {
                long numRows = dbmsConfiguration.isNumRowsEnabled() ? tableService.fetchNumRows(dbmsSql.getSelectRowCountSql(),db, table) : -1;
                table.setNumRows(numRows);
            }

            synchronized (db.getTablesMap()) {
                db.getTablesMap().put(table.getName(), table);
            }

            listener.gatheringDetailsProgressed(table);

            LOGGER.debug("Retrieved details of {}", table.getFullName());
        }

        /**
         * Wait for all of the tables to be created.
         * By default this does nothing since this implementation isn't threaded.
         */
        void join() {
        }
    }

    /**
     * Multi-threaded implementation of a class that creates tables
     */
    private class ThreadedTableCreator extends TableCreator {
        private final Set<Thread> threads = new HashSet<>();
        private final int maxThreads;

        ThreadedTableCreator(int maxThreads) {
            this.maxThreads = maxThreads;
        }

        @Override
        void create(Database db, BasicTableMeta tableMeta, ProgressListener listener) {
            Thread runner = new Thread() {
                @Override
                public void run() {
                    try {
                        createImpl(db, tableMeta, listener);
                    } catch (SQLException exc) {
                        LOGGER.error("SQL exception",exc);
                    } finally {
                        synchronized (threads) {
                            threads.remove(this);
                            threads.notify();
                        }
                    }
                }
            };

            synchronized (threads) {
                // wait for enough 'room'
                while (threads.size() >= maxThreads) {
                    try {
                        threads.wait();
                    } catch (InterruptedException interrupted) {
                    }
                }

                threads.add(runner);
            }

            runner.start();
        }

        /**
         * Wait for all of the started threads to complete
         */
        @Override
        public void join() {
            while (true) {
                Thread thread;

                synchronized (threads) {
                    Iterator<Thread> iter = threads.iterator();
                    if (!iter.hasNext())
                        break;

                    thread = iter.next();
                }

                try {
                    thread.join();
                } catch (InterruptedException exc) {
                }
            }
        }
    }

    /**
     * Return a list of basic details of the tables in the schema.
     *
     * @param forTables true if we're getting table data, false if getting view data
     * @return
     * @throws SQLException
     */
    private List<BasicTableMeta> getBasicTableMeta(
            Database db,
            ProgressListener listener,
            boolean forTables,
            String... types) throws SQLException {

        String sql = forTables ? dbmsSql.getSelectTablesSql() : dbmsSql.getSelectViewsSql();
        List<BasicTableMeta> basics = new ArrayList<>();


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
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve " + clazz + " names with custom SQL", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
            }
        }

        if (basics.isEmpty()) {
            String lastTableName = null;
            try (ResultSet rs = sqlService.getDatabaseMetaData().getTables(null, db.getSchema().getName(), "%", types)){
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

                System.out.flush();
                System.err.println();
                System.err.println("Ignoring view " + lastTableName + " due to exception:");
                exc.printStackTrace();
                System.err.println("Continuing analysis.");
            }
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

    private void initCheckConstraints(Database db, ProgressListener listener) {
        String sql = dbmsSql.getSelectCheckConstraintsSql();
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
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve check constraints", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
            }
        }
    }

    private void initColumnTypes(Database db, ProgressListener listener) {
        String sql = dbmsSql.getSelectColumnTypesSql();
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
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve column type details", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
            }
        }
    }

    private void initTableIds(Database db) throws SQLException {
        String sql = dbmsSql.getSelectTableIdsSql();
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
                System.err.println();
                System.err.println(sql);
                throw sqlException;
            }
        }
    }

    private void initIndexIds(Database db) throws SQLException {
        String sql = dbmsSql.getSelectIndexIdsSql();
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
                System.err.println();
                System.err.println(sql);
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
    private void initTableComments(Database db, ProgressListener listener) {
        String sql = dbmsSql.getSelectTableCommentsSql();
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
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve table/view comments", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
            }
        }
    }

    /**
     * Initializes view comments.
     *
     * @throws SQLException
     */
    private void initViewComments(Database db, ProgressListener listener) {
        String sql = dbmsSql.getSelectViewCommentsSql();
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
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve table/view comments", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
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
    private void initTableColumnComments(Database db, ProgressListener listener) {
        String sql = dbmsSql.getSelectColumnCommentsSql();
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
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve column comments", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
            }
        }
    }

    /**
     * Initializes view column comments.
     *
     * @throws SQLException
     */
    private void initViewColumnComments(Database db, ProgressListener listener) {
        String sql = dbmsSql.getSelectViewColumnCommentsSql();
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
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve view column comments", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
            }
        }
    }

    /**
     * Initializes stored procedures / functions.
     *
     * @throws SQLException
     */
    private void initRoutines(Database db, ProgressListener listener) {
        String sql = dbmsSql.getSelectRoutinesSql();

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
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve stored procedure/function details", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
            }
        }

        sql = dbmsSql.getSelectRoutineParametersSql();

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
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve stored procedure/function details", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
            }
        }
    }

}
