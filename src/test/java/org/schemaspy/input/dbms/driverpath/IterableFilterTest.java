package org.schemaspy.input.dbms.driverpath;

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class IterableFilterTest {

  @Test
  void wrapsIterator() {
    Iterator<?> iterator = Mockito.mock(Iterator.class);
    assertThat(
        new IterableFilter(() -> iterator, (t) -> true)
            .iterator()
    ).isNotSameAs(iterator);
  }
}