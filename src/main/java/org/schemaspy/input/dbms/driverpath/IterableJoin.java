package org.schemaspy.input.dbms.driverpath;

import java.util.Arrays;
import java.util.Iterator;

public class IterableJoin<T> implements Iterable<T> {

  private final Iterable<Iterable<T>> iterables;

  public IterableJoin(final Iterable<T>...iterables) {
    this(() -> Arrays.asList(iterables).iterator());
  }
  public IterableJoin(final Iterable<Iterable<T>> iterables) {
    this.iterables = iterables;
  }
  @Override
  public Iterator<T> iterator() {
    return new IteratorJoin<>(
        new IteratorMap<>(
            iterables.iterator(),
            Iterable::iterator
        )
    );
  }
}
