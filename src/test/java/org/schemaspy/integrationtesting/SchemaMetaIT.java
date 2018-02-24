package org.schemaspy.integrationtesting;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.schemaspy.Config;
import org.schemaspy.DbAnalyzer;
import org.schemaspy.app.context.Context;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.xml.SchemaMeta;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.testing.ContextRule;
import org.schemaspy.testing.H2MemoryRule;
import org.schemaspy.util.LineWriter;
import org.schemaspy.view.DotFormatter;
import org.schemaspy.view.WriteStats;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SchemaMetaIT {

    private static String BY_SCRIPT_COMMENT = "Set by script";
    private static String BY_SCHEMA_META_COMMENT = "Set from SchemaMeta";

    public static H2MemoryRule h2MemoryRule = new H2MemoryRule("SchemaMetaIT").addSqlScript("src/test/resources/integrationTesting/schemaMetaIT/dbScripts/shemaMetaIT.h2.sql");

    private static String[] args = {
            "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
            "-db", "SchemaMetaIT",
            "-s", "SCHEMAMETAIT",
            "-cat", "SCHEMAMETAIT",
            "-o", "target/integrationtesting/schemaMetaIT",
            "-u", "sa"
    };

    public static ContextRule contextRule = new ContextRule(args);

    @ClassRule
    public static final TestRule chain = RuleChain
            .outerRule(h2MemoryRule)
            .around(contextRule);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static final ProgressListener progressListener = mock(ProgressListener.class);

    private Context context;
    private Config config;
    private DatabaseMetaData databaseMetaData;
    private String schema;
    private String catalog;
    private DatabaseService databaseService;

    @Before
    public void setup() throws IOException, SQLException {
        context = contextRule.getContext();
        config = new Config(context.getArgs());
        databaseMetaData = context.getSqlService().connect(config);
        schema = context.getCommandLineArguments().getSchema();
        catalog = context.getCommandLineArguments().getCatalog();
        databaseService = context.getDatabaseService();
    }

    @Test
    public void commentsNullTableComment() throws Exception {
        Database database = new Database(null, databaseMetaData, "DatabaseServiceIT", catalog, schema, null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/nullTableComment.xml","SchemaMetaIT", schema);
        Database databaseWithSchemaMeta = new Database(null, databaseMetaData, "SchemaMetaIT", catalog, schema, schemaMeta, progressListener);
        databaseService.gatheringSchemaDetails(config, databaseWithSchemaMeta, progressListener);

        assertThat(database.getTables().size()).isGreaterThan(0);
        assertThat(database.getSchema().getComment()).isEqualToIgnoringCase(BY_SCRIPT_COMMENT);
        assertThat(database.getTablesByName().get("ACCOUNT").getComments()).isEqualToIgnoringCase(BY_SCRIPT_COMMENT);
        assertThat(database.getTablesByName().get("ACCOUNT").getColumn("name").getComments()).isEqualToIgnoringCase(BY_SCRIPT_COMMENT);

        assertThat(databaseWithSchemaMeta.getTables().size()).isGreaterThan(0);
        assertThat(databaseWithSchemaMeta.getSchema().getComment()).isEqualToIgnoringCase(BY_SCRIPT_COMMENT);
        assertThat(databaseWithSchemaMeta.getTablesByName().get("ACCOUNT").getComments()).isEqualToIgnoringCase(BY_SCRIPT_COMMENT);
        assertThat(databaseWithSchemaMeta.getTablesByName().get("ACCOUNT").getColumn("name").getComments()).isEqualToIgnoringCase(BY_SCRIPT_COMMENT);
        assertThat(databaseWithSchemaMeta.getTablesByName().get("ACCOUNT").getColumn("accountId").getComments()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
    }

    @Test
    public void commentsNoTableComment() throws SQLException {
        Database database = new Database(null, databaseMetaData, "DatabaseServiceIT", catalog, schema, null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/noTableComment.xml","SchemaMetaIT", schema);
        Database databaseWithSchemaMeta = new Database(null, databaseMetaData, "SchemaMetaIT", catalog, schema, schemaMeta, progressListener);
        databaseService.gatheringSchemaDetails(config, databaseWithSchemaMeta, progressListener);

        assertThat(database.getTables().size()).isGreaterThan(0);
        assertThat(database.getTablesByName().get("ACCOUNT").getColumn("accountId").getComments()).isNull();

        assertThat(databaseWithSchemaMeta.getTables().size()).isGreaterThan(0);
        assertThat(databaseWithSchemaMeta.getTablesByName().get("ACCOUNT").getColumn("accountId").getComments()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
    }

    @Test
    @Ignore
    //Reported as issue #199
    public void commentsAreReplacedWithReplaceComments() throws Exception {
        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/replaceComments.xml","SchemaMetaIT", schema);
        Database database = new Database(null, databaseMetaData, "SchemaMetaIT", catalog, schema, schemaMeta, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);

        assertThat(database.getTables().size()).isGreaterThan(0);
        assertThat(database.getSchema().getComment()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
        assertThat(database.getTablesByName().get("ACCOUNT").getComments()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
        assertThat(database.getTablesByName().get("ACCOUNT").getColumn("name").getComments()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
    }

    @Test
    public void remoteTable() throws Exception {
        Database database = new Database(null, databaseMetaData, "SchemaMetaIT", catalog, schema, null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/remoteTable.xml","SchemaMetaIT", schema);
        Database databaseWithSchemaMeta = new Database(null, databaseMetaData, "SchemaMetaIT", catalog, schema, schemaMeta, progressListener);
        databaseService.gatheringSchemaDetails(config, databaseWithSchemaMeta, progressListener);

        assertThat(database.getRemoteTables().size()).isLessThan(databaseWithSchemaMeta.getRemoteTables().size());
        assertThat(database.getRemoteTablesMap().get("other.other.CONTRACT")).isNull();
        assertThat(databaseWithSchemaMeta.getRemoteTablesMap().get("other.other.CONTRACT")).isNotNull();
    }

    @Test
    public void remoteTableAndRelationShip() throws Exception {
        Database database = new Database(null, databaseMetaData, "SchemaMetaIT", catalog, schema, null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/remoteTable.xml","SchemaMetaIT", schema);
        Database databaseWithSchemaMeta = new Database(null, databaseMetaData, "SchemaMetaIT", catalog, schema, schemaMeta, progressListener);
        databaseService.gatheringSchemaDetails(config, databaseWithSchemaMeta, progressListener);

        assertThat(database.getTablesByName().get("ACCOUNT").getNumChildren())
                .isLessThan(databaseWithSchemaMeta.getTablesByName().get("ACCOUNT").getNumChildren());
    }

    @Test
    public void addColumn() throws Exception {
        Database database = new Database(null, databaseMetaData, "SchemaMetaIT", catalog, schema, null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/addColumn.xml","SchemaMetaIT", schema);
        Database databaseWithSchemaMeta = new Database(null, databaseMetaData, "SchemaMetaIT", catalog, schema, schemaMeta, progressListener);
        databaseService.gatheringSchemaDetails(config, databaseWithSchemaMeta, progressListener);

        assertThat(database.getTablesByName().get("ACCOUNT").getColumns().size())
                .isLessThan(databaseWithSchemaMeta.getTablesByName().get("ACCOUNT").getColumns().size());
    }

    @Test
    public void disableImpliedOnAgentAccountId() throws Exception {
        Database database = new Database(null, databaseMetaData, "SchemaMetaIT", catalog, schema, null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/disableImpliedOnAgent.xml","SchemaMetaIT", schema);
        Database databaseWithSchemaMeta = new Database(null, databaseMetaData, "SchemaMetaIT", catalog, schema, schemaMeta, progressListener);
        databaseService.gatheringSchemaDetails(config, databaseWithSchemaMeta, progressListener);

        DbAnalyzer.getImpliedConstraints(database.getTables());
        DbAnalyzer.getImpliedConstraints(databaseWithSchemaMeta.getTables());

        assertThat(database.getTablesByName().get("ACCOUNT").getNumChildren())
                .isGreaterThan(databaseWithSchemaMeta.getTablesByName().get("ACCOUNT").getNumChildren());
    }

    @Test
    public void addFKInsteadOfImplied() throws Exception {
        Database database = new Database(null, databaseMetaData, "SchemaMetaIT", catalog, schema, null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/addFKInsteadOfImplied.xml","SchemaMetaIT", schema);
        Database databaseWithSchemaMeta = new Database(null, databaseMetaData, "SchemaMetaIT", catalog, schema, schemaMeta, progressListener);
        databaseService.gatheringSchemaDetails(config, databaseWithSchemaMeta, progressListener);

        assertThat(database.getTablesByName().get("ACCOUNT").getNumChildren())
                .isLessThan(databaseWithSchemaMeta.getTablesByName().get("ACCOUNT").getNumChildren());
    }

    @Test
    public void disableDiagramAssociations() throws Exception {
        Database database = new Database(null, databaseMetaData, "SchemaMetaIT", catalog, schema, null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/disableDiagramAssociations.xml","SchemaMetaIT", schema);
        Database databaseWithSchemaMeta = new Database(null, databaseMetaData, "SchemaMetaIT", catalog, schema, schemaMeta, progressListener);
        databaseService.gatheringSchemaDetails(config, databaseWithSchemaMeta, progressListener);

        File withoutSchemaMetaOutput = temporaryFolder.newFolder("withOutSchemaMeta");
        try (LineWriter lineWriter = new LineWriter(new File(withoutSchemaMetaOutput, "company.dot"),"UTF-8")) {
            DotFormatter.getInstance().writeAllRelationships(database.getTablesByName().get("COMPANY"), false, new WriteStats(database.getTables()), lineWriter, withoutSchemaMetaOutput);
        }
        String dotFileWithoutSchemaMeta = Files.readAllLines(new File(withoutSchemaMetaOutput, "company.dot").toPath()).stream().collect(Collectors.joining());

        File withSchemaMetaOutput = temporaryFolder.newFolder("withSchemaMeta");
        try (LineWriter lineWriter = new LineWriter(new File(withSchemaMetaOutput, "company.dot"),"UTF-8")){
            DotFormatter.getInstance().writeAllRelationships(databaseWithSchemaMeta.getTablesByName().get("COMPANY"), false, new WriteStats(databaseWithSchemaMeta.getTables()), lineWriter, withSchemaMetaOutput);
        }
        String dotFileWithSchemaMeta  = Files.readAllLines(new File(withSchemaMetaOutput, "company.dot").toPath()).stream().collect(Collectors.joining());

        assertThat(dotFileWithoutSchemaMeta).contains("\"COUNTRY\":\"COUNTRYID\"");
        assertThat(dotFileWithSchemaMeta).doesNotContain("\"COUNTRY\":\"COUNTRYID\"");
    }
}
