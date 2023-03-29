package org.schemaspy.input.dbms;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DriverPath {

    private final String driverPath;

    public DriverPath(String driverPath) {
        this.driverPath = driverPath;
    }

    public Set<URI> uris() {
        return Stream
            .of(driverPath.split(File.pathSeparator))
            .map(PathCollection::new)
            .flatMap(PathCollection::stream)
            .map(Path::toUri)
            .collect(Collectors.toSet());
    }
}
