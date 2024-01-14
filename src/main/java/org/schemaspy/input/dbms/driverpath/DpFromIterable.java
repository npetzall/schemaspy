package org.schemaspy.input.dbms.driverpath;

import java.io.File;
import java.nio.file.Path;

import org.schemaspy.input.dbms.DbDriverLoader;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Encapsulates what driver path to use based on connection config.
 */
public final class DpFromIterable implements Driverpath {

    private final Iterable<String> driverPath;

    public DpFromIterable(final Iterable<Path> driverPath) {
        this.driverPath = new IterableMap<>(
            new IterableJoin<>(
                new IterableMap<>(
                    new IterableFilter<>(
                        driverPath,
                        new OnFalsePredicate<>(
                            new PathExist(),
                            new LogPath(
                                "Provided driverPath is missing: '{}'",
                                LoggerFactory.getLogger(DbDriverLoader.class),
                                Level.WARN
                            ))),
                    ExpandDriverPath::new
                )
            ),
            Path::toString
        );
    }

    @Override
    public String value() {
        return String.join(File.pathSeparator, this.driverPath);
    }
}
