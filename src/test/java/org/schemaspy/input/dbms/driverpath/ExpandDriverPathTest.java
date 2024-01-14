package org.schemaspy.input.dbms.driverpath;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpandDriverPathTest {

  private Path testPath = Paths.get("src", "test", "resources", "expandDriverPath");
  @Test
  void expandDriverPathWithSubPaths() {
    assertThat(
        new ExpandDriverPath(testPath)
    ).containsExactly(
        testPath,
        testPath.resolve("emptyDir"),
        testPath.resolve("other"),
        testPath.resolve("other").resolve("otherSub"),
        testPath.resolve("other").resolve("otherSub").resolve("otherSub.properties"),
        testPath.resolve("some.properties")
    );
  }

  @Test
  void expandOnFileReturnsFileOnly() {
    assertThat(
        new ExpandDriverPath(testPath.resolve("some.properties"))
    ).containsExactly(
        testPath.resolve("some.properties")
    );
  }

}