package org.schemaspy.input.dbms.dbmsinputsource;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.schemaspy.input.dbms.DbmsInputSource;
import org.schemaspy.input.dbms.testing.DbmsInputConfigurationFluent;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.testcontainers.containers.MySQLContainer;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MysqlKeyWordTableIT {

    @ClassRule
    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MySQLContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/dbScripts/mysql_keyword.sql");

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
                .setDatabaseName("test")
                .setSchema("test")
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
    public void hasATableNamedDistinct() {
        assertThat(database.getTables()).extracting(t -> t.getName()).contains("DISTINCT");
    }
}
