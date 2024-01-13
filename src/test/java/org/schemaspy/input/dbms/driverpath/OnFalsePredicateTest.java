package org.schemaspy.input.dbms.driverpath;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OnFalsePredicateTest {

  @Test
  void callsConsumerIfOriginIsFalse() {
    AtomicBoolean executed = new AtomicBoolean(false);
    new OnFalsePredicate<>(a -> false, b -> executed.set(true)).test(new Object());
    assertThat(executed).isTrue();
  }

  @Test
  void doesNotCallConsumerIfOriginIsTrue() {
    AtomicBoolean executed = new AtomicBoolean(false);
    new OnFalsePredicate<>(a -> true, b -> executed.set(true)).test(new Object());
    assertThat(executed).isFalse();
  }

}