package jad;

import static org.objectweb.asm.Opcodes.*;

import java.io.PrintWriter;
import org.objectweb.asm.ClassVisitor;

public final class ClassPrinter extends ClassVisitor {
  private final PrintWriter writer;

  @Override
  public void visitEnd() {
    writer.close();
  }

  private String simple(String name) {
    var i = name.lastIndexOf('/');
    if (i < 0) return name;
    return name.substring(i + 1);
  }

  @Override
  public void visit(
      int version,
      int access,
      String name,
      String signature,
      String superName,
      String[] interfaces) {
    writer.println("<!DOCTYPE html>");
    writer.println("<html lang=\"en\">");
    writer.println("<meta charset=\"utf-8\"/>");
    writer.printf("<title>%s</title>\n", simple(name));
  }

  ClassPrinter(PrintWriter writer) {
    super(ASM9);
    this.writer = writer;
  }
}
