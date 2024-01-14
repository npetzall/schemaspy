package org.schemaspy.input.dbms.driverpath;

import java.nio.file.Path;
import java.util.Iterator;

import org.schemaspy.input.dbms.DbDriverLoader;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Encapsulates what driver path to use based on connection config.
 */
public final class DpFromIterable implements Driverpath {

    private final Iterable<Path> driverPath;

    public DpFromIterable(final Iterable<Path> driverPath) {
        this.driverPath =
            new IterableJoin<>(
                new IterableMap<>(
                    new IterableFilter<>(
                        driverPath,
                        new OnFalsePredicate<>(
                            new PathExist(),
                            new LogPath(
                                "Provided -dp(driverPath) is missing: '{}'",
                                LoggerFactory.getLogger(DbDriverLoader.class),
                                Level.WARN
                            ))),
                    ExpandDriverPath::new
                )
            );
    }

    @Override
    public Iterator<Path> iterator() {
        return driverPath.iterator();
    }
}
