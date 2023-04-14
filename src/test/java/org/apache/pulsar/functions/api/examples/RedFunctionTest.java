package org.apache.pulsar.functions.api.examples;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.pulsar.functions.api.Context;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

class RedFunctionTest {
  @Test
  public void testRedFunction() {
    RedFunction red = new RedFunction();
    String output = red.process("abc", mock(Context.class));
    assertEquals(output, "abc..");
  }
}