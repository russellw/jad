package jad;

import static org.objectweb.asm.Opcodes.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class HtmlPrinter {
  private Set<String> classNames = new HashSet<>();
  private PrintWriter writer;

  private void print(MethodNode methodNode) {}

  private void print(ClassNode classNode) {
    // HTML header
    writer.println("<!DOCTYPE html>");
    writer.println("<html lang=\"en\">");
    writer.println("<meta charset=\"utf-8\"/>");
    writer.printf("<title>%s</title>\n", simple(classNode.name));

    // class header
    if ((classNode.access & ACC_PUBLIC) != 0) writer.print("public ");
    if ((classNode.access & ACC_PRIVATE) != 0) writer.print("private ");
    if ((classNode.access & ACC_PROTECTED) != 0) writer.print("protected ");

    if ((classNode.access & ACC_ABSTRACT) != 0) writer.print("abstract ");
    if ((classNode.access & ACC_FINAL) != 0) writer.print("final ");

    if ((classNode.access & ACC_INTERFACE) != 0) writer.print("interface ");
    else writer.print("class ");

    writer.println(classNode.name);
  }

  private static String simple(String name) {
    var i = name.lastIndexOf('/');
    if (i < 0) return name;
    return name.substring(i + 1);
  }

  HtmlPrinter(Collection<ClassNode> classes) throws FileNotFoundException {
    for (var classNode : classes) classNames.add(classNode.name);
    for (var classNode : classes) {
      try (var writer = new PrintWriter(classNode.name.replace('/', '-') + ".html")) {
        this.writer = writer;
        print(classNode);
      }
    }
  }
}
