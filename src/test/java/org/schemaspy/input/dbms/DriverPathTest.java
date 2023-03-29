package org.schemaspy.input.dbms;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DriverPathTest {

    @Test
    void willAddDirAndContentIfDpIsADirAndNotAFile() {
        URI driverFolder = Paths.get("src", "test", "resources", "driverFolder").toUri();
        URI dummyJarURI = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar").toUri();
        URI dummyNarURI = Paths.get("src", "test", "resources", "driverFolder", "dummy.nar").toUri();
        URI narJarWarNotIncludedURI = Paths.get("src", "test", "resources", "driverFolder", "nar.jar.war.not.included")
            .toUri();

        String dp = Paths.get("src", "test", "resources", "driverFolder").toString();
        Set<URI> uris = new DriverPath(dp).uris();

        assertThat(uris)
            .hasSize(4)
            .contains(driverFolder, dummyJarURI, dummyNarURI, narJarWarNotIncludedURI);
    }

    @Test
    public void willOnlyAddFileIfFileIsSpecified() {
        URI dummyJarURI = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar").toUri();

        String dp = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar").toString();
        Set<URI> uris = new DriverPath(dp).uris();

        assertThat(uris)
            .hasSize(1)
            .contains(dummyJarURI);
    }

    @Test
    public void willAddDirAndContentIfDpSecondArgIsADirAndNotAFile() {
        URI driverFolder = Paths.get("src", "test", "resources", "driverFolder").toUri();
        URI dummyJarURI = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar").toUri();
        URI dummyNarURI = Paths.get("src", "test", "resources", "driverFolder", "dummy.nar").toUri();
        URI narJarWarNotIncludedURI = Paths.get("src", "test", "resources", "driverFolder", "nar.jar.war.not.included")
            .toUri();

        String dpFile = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar").toString();
        String dpDir = Paths.get("src", "test", "resources", "driverFolder").toString();
        Set<URI> uris = new DriverPath(dpFile + File.pathSeparator + dpDir).uris();

        assertThat(uris)
            .hasSize(4)
            .contains(driverFolder, dummyJarURI, dummyNarURI, narJarWarNotIncludedURI);
    }

}