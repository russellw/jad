package jad;

import static org.junit.Assert.*;

import org.junit.Test;

public class EtcTest {
  @Test
  public void ext() {
    assertEquals(Etc.ext("foo.txt"), "txt");
  }
}
