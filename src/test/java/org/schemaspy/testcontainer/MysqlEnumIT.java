package org.schemaspy.testcontainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schemaspy.Main;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.MySQLContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
public class MysqlEnumIT {

    @ClassRule
    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MySQLContainer("mysql:5.5.59"))
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/mysqlenumit/dbScripts/enum.sql")
                    .withInitUser("root","test");

    @BeforeClass
    public static void doCreateDatabaseRepresentation() throws Exception {
        String[] args = {
                "-t", "mysql",
                "-db", "test_enum_schema",
                "-s", "test_enum_schema",
                "-o", "target/integrationtesting/mysqlenumit",
                "-u", "root",
                "-p", "test",
                "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                "-port", jdbcContainerRule.getContainer().getMappedPort(3306).toString()
        };
        Main.main(args);
    }

    @Test
    public void databaseShouldExist() throws IOException {
        String columnsHtml = new String(Files.readAllBytes(Paths.get("target","integrationtesting","mysqlenumit","columns.html")));
        assertThat(columnsHtml).contains("enum(&#39;N&#39;, &#39;Y&#39;)");
    }
}
