package jad;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.Test;
import org.objectweb.asm.ClassReader;

public class ClassPrinterTest {
  @Test
  public void visit() {
    try {
      var classReader = new ClassReader("java.lang.String");
      var stringWriter = new StringWriter();
      var classPrinter = new ClassPrinter(new PrintWriter(stringWriter));
      classReader.accept(classPrinter, 0);
      var s = stringWriter.toString();
      assertTrue(s.contains("String"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
