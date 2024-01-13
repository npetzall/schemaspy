package org.schemaspy.input.dbms.driverpath;

import java.util.Iterator;
import java.util.function.Predicate;

public class IterableFilter<T> implements Iterable<T> {

  private final Iterable<T> origin;
  private final Predicate<T> predicate;

  public IterableFilter(
      final Iterable<T> origin,
      final Predicate<T> predicate
  ) {
    this.origin = origin;
    this.predicate = predicate;
  }

  @Override
  public Iterator<T> iterator() {
    return new IteratorFilter<>(origin.iterator(), predicate);
  }

}
