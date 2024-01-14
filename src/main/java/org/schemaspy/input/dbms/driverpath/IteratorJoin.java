package org.schemaspy.input.dbms.driverpath;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteratorJoin<T> implements Iterator<T> {

  private final Iterator<Iterator<T>> iterators;

  private Iterator<T> current = Collections.emptyIterator();

  public IteratorJoin(final Iterator<T>...iterators) {
    this(Arrays.asList(iterators).iterator());
  }

  public IteratorJoin(final Iterator<Iterator<T>> iterators) {
    this.iterators = iterators;
  }

  @Override
  public boolean hasNext() {
    while (!this.current.hasNext() && this.iterators.hasNext()) {
      this.current = this.iterators.next();
    }
    return this.current.hasNext();
  }

  @Override
  public T next() {
    if (this.hasNext()) {
      return current.next();
    } else {
      throw new NoSuchElementException();
    }
  }
}
