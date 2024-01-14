package org.schemaspy.input.dbms.driverpath;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IteratorJoinTest {

  @Test
  void join() {
    assertThat(
        new IteratorJoin<>(
            List.of("one", "two").iterator(),
            List.of("three", "four").iterator()
        )
    ).toIterable().containsExactly("one", "two", "three", "four");
  }

  @Test
  void emptyIterator() {
    assertThat(new IteratorJoin<>(
        Collections.emptyIterator()
    )
    ).isExhausted();
  }

  @Test
  void throwsNoSuchElementException() {
    IteratorJoin<String> iteratorJoin =
        new IteratorJoin(
            Collections.emptyIterator()
        );
    assertThatThrownBy(
        () -> iteratorJoin.next()
    ).isInstanceOf(NoSuchElementException.class);
  }

}