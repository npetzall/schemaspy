package org.schemaspy.input.dbms.driverpath;

import java.io.File;
import java.nio.file.Path;

/**
 * Encapsulates what driver path to use based on connection config.
 */
public final class DpFromIterable implements Driverpath {

    private final Iterable<String> driverPath;

    public DpFromIterable(final Iterable<Path> driverPath) {
        this.driverPath = new IterableMap<>(driverPath, Path::toString);
    }

    @Override
    public String value() {
        return String.join(File.pathSeparator, this.driverPath);
    }
}
