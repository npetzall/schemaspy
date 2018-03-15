package org.schemaspy.input.dbms.dbmsinputsource;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schemaspy.input.dbms.DbmsInputSource;
import org.schemaspy.input.dbms.testing.DbmsInputConfigurationFluent;
import org.schemaspy.model.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MySQLContainer;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
/*
 https://github.com/schemaspy/schemaspy/pull/174#issuecomment-352158979
 Summary: mysql-connector-java has a bug regarding dots in tablePattern.
 https://bugs.mysql.com/bug.php?id=63992
*/
public class MysqlSpacesIT {

    @ClassRule
    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MySQLContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/dbScripts/mysql_spaces.sql")
                    .withInitUser("root", "test");

    private static Database database;

    @Before
    public synchronized void createDatabaseRepresentation() {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() {
        DbmsInputConfigurationFluent dbmsInputConfigurationFluent = new DbmsInputConfigurationFluent()
                .setDatabaseType("mysql")
                .setDatabaseName("TEST 1.0")
                .setSchema("TEST 1.0")
                .setCatalog("%")
                .setHost(jdbcContainerRule.getContainer().getContainerIpAddress())
                .setPort(jdbcContainerRule.getContainer().getMappedPort(3306).toString())
                .setUser(jdbcContainerRule.getContainer().getUsername())
                .setPassword(jdbcContainerRule.getContainer().getPassword());

        ProgressListener progressListener = mock(ProgressListener.class);
        DbmsInputSource dbmsInputSource = new DbmsInputSource(dbmsInputConfigurationFluent, progressListener);
        this.database = dbmsInputSource.createDatabaseModel();
    }

    @Test
    public void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("TEST 1.0");
    }

    @Test
    public void databaseShouldHaveTable() {
        assertThat(database.getTables()).extracting(Table::getName).contains("TABLE 1.0");
    }

    @Test
    public void tableShouldHavePKWithAutoIncrement() {
        assertThat(database.getTablesByName().get("TABLE 1.0").getColumns()).extracting(TableColumn::getName).contains("id");
        assertThat(database.getTablesByName().get("TABLE 1.0").getColumn("id").isPrimary()).isTrue();
        assertThat(database.getTablesByName().get("TABLE 1.0").getColumn("id").isAutoUpdated()).isTrue();
    }

    @Test
    public void tableShouldHaveForeignKey() {
        assertThat(database.getTablesByName().get("TABLE 1.0").getForeignKeys()).extracting(ForeignKeyConstraint::getName).contains("link fk");
    }

    @Test
    public void tableShouldHaveUniqueKey() {
        assertThat(database.getTablesByName().get("TABLE 1.0").getIndexes()).extracting(TableIndex::getName).contains("name_link_unique");
    }

    @Test
    public void tableShouldHaveColumnWithSpaceInIt() {
        assertThat(database.getTablesByName().get("TABLE 1.0").getColumns()).extracting(TableColumn::getName).contains("link id");
    }
}
