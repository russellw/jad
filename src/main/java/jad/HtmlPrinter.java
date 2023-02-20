package jad;

import static org.objectweb.asm.Opcodes.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public final class HtmlPrinter {
  private Set<String> classNames = new HashSet<>();
  private PrintWriter writer;

  private void print(FieldNode fieldNode) {
    // heading
    var name = fieldNode.name;
    writer.printf("<h2 id=\"%s\">%s</h2>\n", name, name);

    // embellished name
    writer.print("<code>");

    if ((fieldNode.access & ACC_PUBLIC) != 0) writer.print("public ");
    if ((fieldNode.access & ACC_PRIVATE) != 0) writer.print("private ");
    if ((fieldNode.access & ACC_PROTECTED) != 0) writer.print("protected ");

    if ((fieldNode.access & ACC_FINAL) != 0) writer.print("final ");
    if ((fieldNode.access & ACC_STATIC) != 0) writer.print("static ");

    if ((fieldNode.access & ACC_VOLATILE) != 0) writer.print(" ");
    if ((fieldNode.access & ACC_TRANSIENT) != 0) writer.print(" ");

    writer.print(Type.getType(fieldNode.desc).getClassName());
    writer.print(' ');
    writer.print(fieldNode.name);

    writer.print("</code><br>\n");
    writer.print("<br>\n");
  }

  private void print(MethodNode methodNode) {
    // heading
    var name = esc(methodNode.name);
    writer.printf("<h2 id=\"%s\">%s</h2>\n", name, name);

    // embellished name
    writer.print("<code>");

    if ((methodNode.access & ACC_PUBLIC) != 0) writer.print("public ");
    if ((methodNode.access & ACC_PRIVATE) != 0) writer.print("private ");
    if ((methodNode.access & ACC_PROTECTED) != 0) writer.print("protected ");

    if ((methodNode.access & ACC_ABSTRACT) != 0) writer.print("abstract ");
    if ((methodNode.access & ACC_FINAL) != 0) writer.print("final ");
    if ((methodNode.access & ACC_STATIC) != 0) writer.print("static ");

    if ((methodNode.access & ACC_SYNCHRONIZED) != 0) writer.print("synchronized ");
    if ((methodNode.access & ACC_NATIVE) != 0) writer.print("native ");
    if ((methodNode.access & ACC_STRICT) != 0) writer.print("strictfp ");

    writer.print(Type.getReturnType(methodNode.desc).getClassName());
    writer.print(' ');
    writer.print(esc(methodNode.name));

    writer.print('(');
    var more = false;
    for (var type : Type.getArgumentTypes(methodNode.desc)) {
      if (more) writer.print(", ");
      more = true;
      writer.print(type.getClassName());
    }
    writer.print(')');

    writer.print("</code><br>\n");
    writer.print("<br>\n");
  }

  private void print(ClassNode classNode) {
    // HTML header
    writer.print("<!DOCTYPE html>\n");
    writer.print("<html lang=\"en\">\n");
    writer.print("<meta charset=\"utf-8\"/>\n");
    writer.printf("<title>%s</title>\n", simple(classNode.name));
    writer.print("<style>\n");
    writer.print("caption {\n");
    writer.print("text-align: left;\n");
    writer.print("white-space: nowrap;\n");
    writer.print("}\n");
    writer.print("table.bordered, th.bordered, td.bordered {\n");
    writer.print("border: 1px solid;\n");
    writer.print("border-collapse: collapse;\n");
    writer.print("padding: 5px;\n");
    writer.print("}\n");
    writer.print("table.padded, th.padded, td.padded {\n");
    writer.print("padding: 3px;\n");
    writer.print("}\n");
    writer.print("td.fixed {\n");
    writer.print("white-space: nowrap;\n");
    writer.print("}\n");
    writer.print("</style>\n");

    // contents
    writer.print("<h1 id=\"Contents\">Contents</h1>\n");
    writer.print("<ul>\n");
    writer.print("<li><a href=\"#Contents\">Contents</a>\n");
    writer.print("<li><a href=\"#Class header\">Class header</a>\n");
    if (!classNode.fields.isEmpty()) {
      writer.print("<li><a href=\"#Fields\">Fields</a>\n");
      writer.print("<ul>\n");
      for (var fieldNode : classNode.fields) {
        var name = fieldNode.name;
        writer.printf("<li><a href=\"#%s\">%s</a>\n", name, name);
      }
      writer.print("</ul>\n");
    }
    if (!classNode.methods.isEmpty()) {
      writer.print("<li><a href=\"#Methods\">Methods</a>\n");
      writer.print("<ul>\n");
      for (var methodNode : classNode.methods) {
        var name = esc(methodNode.name);
        writer.printf("<li><a href=\"#%s\">%s</a>\n", name, name);
      }
      writer.print("</ul>\n");
    }
    writer.print("</ul>\n");

    // class header
    writer.print("<h1 id=\"Class header\">Class header</h1>\n");

    // embellished name
    writer.print("<code>");

    if ((classNode.access & ACC_PUBLIC) != 0) writer.print("public ");
    if ((classNode.access & ACC_PRIVATE) != 0) writer.print("private ");
    if ((classNode.access & ACC_PROTECTED) != 0) writer.print("protected ");

    if ((classNode.access & ACC_ABSTRACT) != 0) writer.print("abstract ");
    if ((classNode.access & ACC_FINAL) != 0) writer.print("final ");

    if ((classNode.access & ACC_INTERFACE) != 0) writer.print("interface ");
    else writer.print("class ");

    writer.print(classNode.name);

    if (classNode.superName != null && !classNode.superName.equals("java/lang/Object"))
      writer.print(" extends " + classNode.superName);

    if (!classNode.interfaces.isEmpty()) {
      writer.print(" implements");
      for (var s : classNode.interfaces) writer.print(' ' + s);
    }

    writer.print("</code><br>\n");
    writer.print("<br>\n");

    // class node fields
    writer.print("<table class=\"bordered\">\n");

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Version\n");
    writer.print("<td class=\"bordered\"><code>");
    writer.print(classNode.version);
    writer.print("</code>\n");

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Flags\n");
    writer.printf("<td class=\"bordered\"><code>0x%x", classNode.access);
    if ((classNode.access & ACC_PUBLIC) != 0) writer.print(" ACC_PUBLIC");
    if ((classNode.access & ACC_PRIVATE) != 0) writer.print(" ACC_PRIVATE");
    if ((classNode.access & ACC_PROTECTED) != 0) writer.print(" ACC_PROTECTED");
    if ((classNode.access & ACC_FINAL) != 0) writer.print(" ACC_FINAL");
    if ((classNode.access & ACC_SUPER) != 0) writer.print(" ACC_SUPER");
    if ((classNode.access & ACC_INTERFACE) != 0) writer.print(" ACC_INTERFACE");
    if ((classNode.access & ACC_ABSTRACT) != 0) writer.print(" ACC_ABSTRACT");
    if ((classNode.access & ACC_SYNTHETIC) != 0) writer.print(" ACC_SYNTHETIC");
    if ((classNode.access & ACC_ANNOTATION) != 0) writer.print(" ACC_ANNOTATION");
    if ((classNode.access & ACC_ENUM) != 0) writer.print(" ACC_ENUM");
    writer.print("</code>\n");

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Name\n");
    writer.print("<td class=\"bordered\"><code>");
    writer.print(classNode.name);
    writer.print("</code>\n");

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Signature\n");
    writer.print("<td class=\"bordered\"><code>");
    writer.print(classNode.signature);
    writer.print("</code>\n");

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Super\n");
    writer.print("<td class=\"bordered\"><code>");
    writer.print(classNode.superName);
    writer.print("</code>\n");

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Source file\n");
    writer.print("<td class=\"bordered\"><code>");
    writer.print(classNode.sourceFile);
    writer.print("</code>\n");

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Source debug\n");
    writer.print("<td class=\"bordered\"><code>");
    writer.print(classNode.sourceDebug);
    writer.print("</code>\n");

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Outer class\n");
    writer.print("<td class=\"bordered\"><code>");
    writer.print(classNode.outerClass);
    writer.print("</code>\n");

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Outer method\n");
    writer.print("<td class=\"bordered\"><code>");
    writer.print(classNode.outerMethod);
    writer.print("</code>\n");

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Outer method desc\n");
    writer.print("<td class=\"bordered\"><code>");
    writer.print(classNode.outerMethodDesc);
    writer.print("</code>\n");

    writer.print("</table>\n");

    // fields
    if (!classNode.fields.isEmpty()) writer.print("<h1 id=\"Fields\">Fields</h1>\n");
    for (var fieldNode : classNode.fields) print(fieldNode);

    // methods
    if (!classNode.methods.isEmpty()) writer.print("<h1 id=\"Methods\">Methods</h1>\n");
    for (var methodNode : classNode.methods) print(methodNode);
  }

  private static String esc(String s) {
    s = s.replace("<", "&lt;");
    s = s.replace(">", "&gt;");
    return s;
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
