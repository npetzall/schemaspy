package org.schemaspy.input.dbms.driverpath;

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class IterableJoinTest {

  @Test
  void wrapsOrigins() {
    Iterator iteratorA = Mockito.mock(Iterator.class);
    Iterator iteratorB = Mockito.mock(Iterator.class);
    assertThat(
        new IterableJoin<>(() -> iteratorB, () -> iteratorB)
            .iterator()
    )
        .isNotSameAs(iteratorA)
        .isNotSameAs(iteratorB);
  }
}