package jad;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;

public final class HtmlPrinter {
  private Set<String> classNames = new HashSet<>();
  private PrintWriter writer;

  private void print(ClassNode classNode) {
    // HTML header
    writer.println("<!DOCTYPE html>");
    writer.println("<html lang=\"en\">");
    writer.println("<meta charset=\"utf-8\"/>");
    writer.printf("<title>%s</title>\n", simple(classNode.name));
  }

  private static String simple(String name) {
    var i = name.lastIndexOf('/');
    if (i < 0) return name;
    return name.substring(i + 1);
  }

  HtmlPrinter(Collection<ClassNode> classes) throws FileNotFoundException {
    for (var classNode : classes) classNames.add(classNode.name);
    for (var classNode : classes) {
      writer = new PrintWriter(classNode.name.replace('/', '-') + ".html");
      print(classNode);
      writer.close();
    }
  }
}
