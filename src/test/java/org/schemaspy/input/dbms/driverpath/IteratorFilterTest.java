package org.schemaspy.input.dbms.driverpath;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IteratorFilterTest {

  @Test
  void willFilter() {
    assertThat(
        new IteratorFilter<>(List.of("one", "two").iterator(), "one"::equals)
    ).toIterable()
        .containsExactly("one");
  }

  @Test
  void emptyIterator() {
    assertThat(
        new IteratorFilter<String>(
            Collections.emptyIterator(),
            Predicate.not(String::isEmpty)
        )
    ).isExhausted();
  }

  @Test
  void throwsNoSuchElementException() {
    IteratorFilter<String> iteratorFilter =
        new IteratorFilter<>(
            Collections.emptyIterator(),
            Predicate.not(String::isEmpty)
        );
    assertThatThrownBy(
        () -> iteratorFilter.next()
    ).isInstanceOf(NoSuchElementException.class);
  }


}