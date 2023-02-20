package jad;

import static org.objectweb.asm.Opcodes.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class HtmlPrinter {
  private Set<String> classNames = new HashSet<>();
  private PrintWriter writer;

  private void print(MethodNode methodNode) {
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
    writer.print(methodNode.name);

    writer.print('(');
    var more = false;
    for (var type : Type.getArgumentTypes(methodNode.desc)) {
      if (more) writer.print(", ");
      more = true;
      writer.print(type.getClassName());
    }
    writer.print(')');

    writer.print('\n');
  }

  private void print(ClassNode classNode) {
    // HTML header
    writer.print("<!DOCTYPE html>\n");
    writer.print("<html lang=\"en\">\n");
    writer.print("<meta charset=\"utf-8\"/>\n");
    writer.printf("<title>%s</title>\n", simple(classNode.name));

    // class header
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

    writer.print("</code>\n");

    // attributes
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

    // methods
    for (var methodNode : classNode.methods) print(methodNode);
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
