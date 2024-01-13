package org.schemaspy.input.dbms.driverpath;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IteratorMapTest {

  @Test
  void willMap() {
    assertThat(
        new IteratorMap<>(
            List.of("one", "two")
                .iterator(),
            String::toUpperCase
        )
    ).toIterable()
        .containsExactly("ONE", "TWO");
  }

  @Test
  void emptyIterator() {
    assertThat(
        new IteratorMap<String,String>(
            Collections.emptyIterator(),
            String::toUpperCase
        )
    ).isExhausted();
  }

  @Test
  void throwsNoSuchElementException() {
    IteratorMap<String,String> iteratorMap =
        new IteratorMap<>(
            Collections.emptyIterator(),
            String::toUpperCase
        );
    assertThatThrownBy(
        () -> iteratorMap.next()
    ).isInstanceOf(NoSuchElementException.class);
  }

}