package org.schemaspy.app.integrationtesting;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schemaspy.app.Main;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.MySQLContainer;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
public class MysqlHTMLIT {

    private static URL expectedXML = MysqlHTMLIT.class.getResource("/integrationTesting/expecting/mysqlhtml/test.test.xml");
    private static URL expectedDeletionOrder = MysqlHTMLIT.class.getResource("/integrationTesting/expecting/mysqlhtml/deletionOrder.txt");
    private static URL expectedInsertionOrder = MysqlHTMLIT.class.getResource("/integrationTesting/expecting/mysqlhtml/insertionOrder.txt");

    @ClassRule
    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new JdbcContainerRule<MySQLContainer>(() -> new MySQLContainer<>("mysql:5.7.18"))
                    .assumeDockerIsPresent().withAssumptions(assumeDriverIsPresent())
                    .withQueryString("?useSSL=false")
                    .withInitScript("integrationTesting/dbScripts/mysql_html_implied_relationship.sql");

    @BeforeClass
    public static void generateHTML() throws Exception {
        MySQLContainer container = jdbcContainerRule.getContainer();
        String[] args = new String[]{
                "-t", "mysql",
                "-db", "test",
                "-s", "test",
                "-host", container.getContainerIpAddress() + ":" + String.valueOf(container.getMappedPort(3306)),
                "-port", String.valueOf(container.getMappedPort(3306)),
                "-u", container.getUsername(),
                "-p", container.getPassword(),
                "-o", "target/mysqlhtml",
                "-connprops", "useSSL\\=false"
        };
        Main.main(args);
    }

    @Test
    public void verifyXML() {
        Diff d = DiffBuilder.compare(Input.fromURL(expectedXML))
                .withTest(Input.fromFile("target/mysqlhtml/test.test.xml"))
                .build();
        assertThat(d.getDifferences()).isEmpty();
    }

    @Test
    public void verifyDeletionOrder() throws IOException {
        assertThat(Files.newInputStream(Paths.get("target/mysqlhtml/deletionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedDeletionOrder.openStream());
    }

    @Test
    public void verifyInsertionOrder() throws IOException {
        assertThat(Files.newInputStream(Paths.get("target/mysqlhtml/insertionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedInsertionOrder.openStream());
    }

    @Test
    public void producesSameContent() throws IOException {
        String target = "target/mysqlhtml";
        Path expectedPath = Paths.get("src/test/resources/integrationTesting/expecting/mysqlhtml");
        List<Path> expectations;
        try (Stream<Path> pathStream = Files.find(expectedPath, 5, (p, a) -> a.isRegularFile())) {
            expectations = pathStream.collect(Collectors.toList());
        }
        assertThat(expectations.size()).isGreaterThan(0);
        for (Path expect : expectations) {
            List<String> expectLines = Files.readAllLines(expect, StandardCharsets.UTF_8);
            Path actual = Paths.get(target, expectedPath.relativize(expect).toString());
            List<String> actualLines = Files.readAllLines(actual, StandardCharsets.UTF_8);
            assertThat(actualLines).as("%s doesn't have the expected number of lines: %s", actual.toString(), expectLines.size()).hasSameSizeAs(expectLines);
            assertThat(actualLines).usingElementComparator((a, e) -> {
                if (e.startsWith("@@IGNORE")) {
                    return 0;
                }
                return a.compareTo(e);
            }).containsAll(expectLines);
        }

    }
}
