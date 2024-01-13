package org.schemaspy.input.dbms.driverpath;

import java.util.Iterator;
import java.util.function.Function;

public class IterableMap<T,R> implements Iterable<R> {

  private final Iterable<T> origin;
  private final Function<T,R> mapFunction;

  public IterableMap(
      final Iterable<T> origin,
      final Function<T, R> mapFunction
  ) {
    this.origin = origin;
    this.mapFunction = mapFunction;
  }

  @Override
  public Iterator<R> iterator() {
    return new IteratorMap(origin.iterator(), mapFunction);
  }
}
