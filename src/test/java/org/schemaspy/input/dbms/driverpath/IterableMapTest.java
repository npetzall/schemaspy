package org.schemaspy.input.dbms.driverpath;

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class IterableMapTest {

  @Test
  void wrapsOrigin() {
    Iterator<?> iterator = Mockito.mock(Iterator.class);
    assertThat(
        new IterableMap<>(() -> iterator, b -> b)
            .iterator()
    ).isNotSameAs(iterator);
  }

}