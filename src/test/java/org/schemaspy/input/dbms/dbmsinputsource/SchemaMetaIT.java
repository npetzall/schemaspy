package org.schemaspy.input.dbms.dbmsinputsource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.schemaspy.DbAnalyzer;
import org.schemaspy.input.dbms.DbmsInputSource;
import org.schemaspy.input.dbms.testing.DbmsInputConfigurationFluent;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.testing.H2MemoryRule;
import org.schemaspy.util.LineWriter;
import org.schemaspy.view.DotFormatter;
import org.schemaspy.view.WriteStats;

import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SchemaMetaIT {

    private static String BY_SCRIPT_COMMENT = "Set by script";
    private static String BY_SCHEMA_META_COMMENT = "Set from SchemaMeta";

    @ClassRule
    public static H2MemoryRule h2MemoryRule = new H2MemoryRule("SchemaMetaIT").addSqlScript("src/test/resources/integrationTesting/schemaMetaIT/dbScripts/shemaMetaIT.h2.sql");

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static ProgressListener progressListener = mock(ProgressListener.class);

    private DbmsInputConfigurationFluent dbmsInputConfigurationFluent;

    @Before
    public void setup() throws SQLException {
        dbmsInputConfigurationFluent = new DbmsInputConfigurationFluent()
                .setDatabaseType("src/test/resources/integrationTesting/dbTypes/h2memory")
                .setDatabaseName("SchemaMetaIT")
                .setSchema(h2MemoryRule.getConnection().getSchema())
                .setCatalog(h2MemoryRule.getConnection().getCatalog())
                .setUser("sa");
    }

    @Test
    public void commentsNullTableComment() {
        Database database = new DbmsInputSource(dbmsInputConfigurationFluent, progressListener).createDatabaseModel();
        Database databaseWithSchemaMeta = new DbmsInputSource(
                dbmsInputConfigurationFluent.setSchemaMetaPath("src/test/resources/integrationTesting/schemaMetaIT/input/nullTableComment.xml"),
                progressListener
        ).createDatabaseModel();

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
    public void commentsNoTableComment() {
        Database database = new DbmsInputSource(dbmsInputConfigurationFluent, progressListener).createDatabaseModel();
        Database databaseWithSchemaMeta = new DbmsInputSource(
                dbmsInputConfigurationFluent.setSchemaMetaPath("src/test/resources/integrationTesting/schemaMetaIT/input/noTableComment.xml"),
                progressListener
        ).createDatabaseModel();

        assertThat(database.getTables().size()).isGreaterThan(0);
        assertThat(database.getTablesByName().get("ACCOUNT").getColumn("accountId").getComments()).isNull();

        assertThat(databaseWithSchemaMeta.getTables().size()).isGreaterThan(0);
        assertThat(databaseWithSchemaMeta.getTablesByName().get("ACCOUNT").getColumn("accountId").getComments()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
    }

    @Test
    public void commentsAreReplacedWithReplaceComments() {
        Database database = new DbmsInputSource(
                dbmsInputConfigurationFluent.setSchemaMetaPath("src/test/resources/integrationTesting/schemaMetaIT/input/replaceComments.xml"),
                progressListener
        ).createDatabaseModel();

        assertThat(database.getTables().size()).isGreaterThan(0);
        assertThat(database.getSchema().getComment()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
        assertThat(database.getTablesByName().get("ACCOUNT").getComments()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
        assertThat(database.getTablesByName().get("ACCOUNT").getColumn("name").getComments()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
    }

    @Test
    public void remoteTable() {
        Database database = new DbmsInputSource(dbmsInputConfigurationFluent, progressListener).createDatabaseModel();
        Database databaseWithSchemaMeta = new DbmsInputSource(
                dbmsInputConfigurationFluent.setSchemaMetaPath("src/test/resources/integrationTesting/schemaMetaIT/input/remoteTable.xml"),
                progressListener
        ).createDatabaseModel();

        assertThat(database.getRemoteTables().size()).isLessThan(databaseWithSchemaMeta.getRemoteTables().size());
        assertThat(database.getRemoteTablesMap().get("other.other.CONTRACT")).isNull();
        assertThat(databaseWithSchemaMeta.getRemoteTablesMap().get("other.other.CONTRACT")).isNotNull();
    }

    @Test
    public void remoteTableAndRelationShip() {
        Database database = new DbmsInputSource(dbmsInputConfigurationFluent, progressListener).createDatabaseModel();
        Database databaseWithSchemaMeta = new DbmsInputSource(
                dbmsInputConfigurationFluent.setSchemaMetaPath("src/test/resources/integrationTesting/schemaMetaIT/input/remoteTable.xml"),
                progressListener
        ).createDatabaseModel();

        assertThat(database.getTablesByName().get("ACCOUNT").getNumChildren())
                .isLessThan(databaseWithSchemaMeta.getTablesByName().get("ACCOUNT").getNumChildren());
    }

    @Test
    public void addColumn() {
        Database database = new DbmsInputSource(dbmsInputConfigurationFluent, progressListener).createDatabaseModel();
        Database databaseWithSchemaMeta = new DbmsInputSource(
                dbmsInputConfigurationFluent.setSchemaMetaPath("src/test/resources/integrationTesting/schemaMetaIT/input/addColumn.xml"),
                progressListener
        ).createDatabaseModel();

        assertThat(database.getTablesByName().get("ACCOUNT").getColumns().size())
                .isLessThan(databaseWithSchemaMeta.getTablesByName().get("ACCOUNT").getColumns().size());
    }

    @Test
    public void disableImpliedOnAgentAccountId() {
        Database database = new DbmsInputSource(dbmsInputConfigurationFluent, progressListener).createDatabaseModel();
        Database databaseWithSchemaMeta = new DbmsInputSource(
                dbmsInputConfigurationFluent.setSchemaMetaPath("src/test/resources/integrationTesting/schemaMetaIT/input/disableImpliedOnAgent.xml"),
                progressListener
        ).createDatabaseModel();

        DbAnalyzer.getImpliedConstraints(database.getTables());
        DbAnalyzer.getImpliedConstraints(databaseWithSchemaMeta.getTables());

        assertThat(database.getTablesByName().get("ACCOUNT").getNumChildren())
                .isGreaterThan(databaseWithSchemaMeta.getTablesByName().get("ACCOUNT").getNumChildren());
    }

    @Test
    public void addFKInsteadOfImplied() {
        Database database = new DbmsInputSource(dbmsInputConfigurationFluent, progressListener).createDatabaseModel();
        Database databaseWithSchemaMeta = new DbmsInputSource(
                dbmsInputConfigurationFluent.setSchemaMetaPath("src/test/resources/integrationTesting/schemaMetaIT/input/addFKInsteadOfImplied.xml"),
                progressListener
        ).createDatabaseModel();

        assertThat(database.getTablesByName().get("ACCOUNT").getNumChildren())
                .isLessThan(databaseWithSchemaMeta.getTablesByName().get("ACCOUNT").getNumChildren());
    }

    @Test
    public void disableDiagramAssociations() throws Exception {
        Database database = new DbmsInputSource(dbmsInputConfigurationFluent, progressListener).createDatabaseModel();
        Database databaseWithSchemaMeta = new DbmsInputSource(
                dbmsInputConfigurationFluent.setSchemaMetaPath("src/test/resources/integrationTesting/schemaMetaIT/input/disableDiagramAssociations.xml"),
                progressListener
        ).createDatabaseModel();

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
