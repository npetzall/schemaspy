package org.schemaspy.input.dbms.driverpath;

import java.util.Iterator;
import java.util.function.Function;

public class IteratorMap<T,R> implements Iterator<R> {

  private final Iterator<T> origin;
  private final Function<T, R> mapFunction;

  public IteratorMap(
      final Iterator<T> origin,
      final Function<T, R> mapFunction
  ) {
    this.origin = origin;
    this.mapFunction = mapFunction;
  }

  @Override
  public boolean hasNext() {
    return origin.hasNext();
  }

  @Override
  public R next() {
    return mapFunction.apply(origin.next());
  }
}
