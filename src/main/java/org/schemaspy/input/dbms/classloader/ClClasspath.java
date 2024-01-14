package org.schemaspy.input.dbms.classloader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.StreamSupport;

import org.schemaspy.input.dbms.driverpath.Driverpath;
import org.schemaspy.input.dbms.driverpath.IterableFilter;
import org.schemaspy.input.dbms.driverpath.IterableMap;

/**
 * Encapsulates how to access the classloader specified by a classpath.
 */
public final class ClClasspath implements ClassloaderSource {

    private final Iterable<URL> classPath;

    public ClClasspath(final Driverpath driverpath) {
        this.classPath = new IterableFilter<>(
            new IterableMap<>(
                new IterableMap<>(
                    driverpath, Path::toUri
                ), uri -> {
                try {
                    return uri.toURL();
                } catch (MalformedURLException e) {
                    return null;
                }
            }
            ),
            Objects::nonNull
        );
    }

    @Override
    public ClassLoader classloader() {
        final URL[] urls = StreamSupport
            .stream(classPath.spliterator(), false)
            .toArray(URL[]::new);

        // if a classpath has been specified then use it to find the driver,
        // otherwise use whatever was used to load this class.
        // thanks to Bruno Leonardo Gonalves for this implementation that he
        // used to resolve issues when running under Maven

        return new URLClassLoader(
                urls,
                new ClDefault().classloader()
        );
    }
}
