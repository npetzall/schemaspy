package org.schemaspy.input.dbms;

import org.schemaspy.input.dbms.exceptions.RuntimeIOException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PathCollection implements Iterable<Path> {
    private final String startPath;
    private final BiPredicate<Path,BasicFileAttributes> matcher;

    public PathCollection(String startPath) {
        this(startPath, (path, attrs) -> true);
    }

    public PathCollection(String startPath, BiPredicate<Path, BasicFileAttributes> matcher) {
        this.startPath = startPath;
        this.matcher = matcher;
    }

    public Set<Path> paths() {
        try(Stream<Path> pathStream = Files.find(Paths.get(startPath), Integer.MAX_VALUE, matcher)) {
            return pathStream.collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeIOException("Failed to collect files under '" + startPath + "'", e);
        }
    }

    @Override
    public Iterator<Path> iterator() {
        return paths().iterator();
    }

    public Stream<Path> stream() {
        return paths().stream();
    }
}
