package org.schemaspy.input.dbms.driverpath;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class OnFalsePredicate<T> implements Predicate<T> {

  private final Predicate<T> origin;
  private final Consumer<T> onFalse;

  public OnFalsePredicate(
      final Predicate<T> origin,
      final Consumer<T> onFalse
  ) {
    this.origin = origin;
    this.onFalse = onFalse;
  }

  @Override
  public boolean test(final T t) {
    if (this.origin.test(t)) {
      return true;
    } else {
      this.onFalse.accept(t);
      return false;
    }
  }
}
