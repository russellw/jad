package jad;

import static org.objectweb.asm.Opcodes.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public final class HtmlPrinter {
  private Set<String> classNames = new HashSet<>();
  private PrintWriter writer;

  private void print(AbstractInsnNode abstractInsn) {}

  private void print(MethodNode method) {
    // heading
    var name = esc(method.name);
    writer.printf("<h2 id=\"%s\">%s</h2>\n", name, name);

    // embellished name
    if ((method.access & ACC_PUBLIC) != 0) writer.print("public ");
    if ((method.access & ACC_PRIVATE) != 0) writer.print("private ");
    if ((method.access & ACC_PROTECTED) != 0) writer.print("protected ");

    if ((method.access & ACC_ABSTRACT) != 0) writer.print("abstract ");
    if ((method.access & ACC_FINAL) != 0) writer.print("final ");
    if ((method.access & ACC_STATIC) != 0) writer.print("static ");

    if ((method.access & ACC_SYNCHRONIZED) != 0) writer.print("synchronized ");
    if ((method.access & ACC_NATIVE) != 0) writer.print("native ");
    if ((method.access & ACC_STRICT) != 0) writer.print("strictfp ");

    writer.print(Type.getReturnType(method.desc).getClassName());
    writer.print(' ');
    writer.print(esc(method.name));

    writer.print('(');
    var more = false;
    for (var type : Type.getArgumentTypes(method.desc)) {
      if (more) writer.print(", ");
      more = true;
      writer.print(type.getClassName());
    }
    writer.print(')');

    if (Etc.some(method.exceptions)) {
      writer.print(" throws");
      more = false;
      for (var s : method.exceptions) {
        if (more) writer.print(',');
        more = true;
        writer.print(' ');
        writer.print(s);
      }
    }

    writer.print("<br>\n");
    writer.print("<br>\n");

    // method node fields
    writer.print("<table class=\"bordered\">\n");

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Flags\n");
    writer.printf("<td class=\"bordered\">0x%x", method.access);
    if ((method.access & ACC_PUBLIC) != 0) writer.print(" ACC_PUBLIC");
    if ((method.access & ACC_PRIVATE) != 0) writer.print(" ACC_PRIVATE");
    if ((method.access & ACC_PROTECTED) != 0) writer.print(" ACC_PROTECTED");
    if ((method.access & ACC_STATIC) != 0) writer.print(" ACC_STATIC");
    if ((method.access & ACC_FINAL) != 0) writer.print(" ACC_FINAL");
    if ((method.access & ACC_SYNCHRONIZED) != 0) writer.print(" ACC_SYNCHRONIZED");
    if ((method.access & ACC_BRIDGE) != 0) writer.print(" ACC_BRIDGE");
    if ((method.access & ACC_VARARGS) != 0) writer.print(" ACC_VARARGS");
    if ((method.access & ACC_NATIVE) != 0) writer.print(" ACC_NATIVE");
    if ((method.access & ACC_ABSTRACT) != 0) writer.print(" ACC_ABSTRACT");
    if ((method.access & ACC_STRICT) != 0) writer.print(" ACC_STRICT");
    if ((method.access & ACC_SYNTHETIC) != 0) writer.print(" ACC_SYNTHETIC");
    if ((method.access & ACC_DEPRECATED) != 0) writer.print(" ACC_DEPRECATED");
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Name\n");
    writer.print("<td class=\"bordered\">");
    writer.print(method.name);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Desc\n");
    writer.print("<td class=\"bordered\">");
    writer.print(method.desc);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Signature\n");
    writer.print("<td class=\"bordered\">");
    writer.print(method.signature);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Annotation default\n");
    writer.print("<td class=\"bordered\">");
    writer.print(method.annotationDefault);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Max stack\n");
    writer.print("<td class=\"bordered\">");
    writer.print(method.maxStack);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Max locals\n");
    writer.print("<td class=\"bordered\">");
    writer.print(method.maxLocals);
    writer.print('\n');

    writer.print("</table>\n");
    // TODO parameters and annotations

    // instructions
    writer.print("<table>\n");
    for (var abstractInsn : method.instructions) print(abstractInsn);
    writer.print("</table>\n");
  }

  private void print(FieldNode field) {
    // heading
    var name = field.name;
    writer.printf("<h2 id=\"%s\">%s</h2>\n", name, name);

    // embellished name
    if ((field.access & ACC_PUBLIC) != 0) writer.print("public ");
    if ((field.access & ACC_PRIVATE) != 0) writer.print("private ");
    if ((field.access & ACC_PROTECTED) != 0) writer.print("protected ");

    if ((field.access & ACC_FINAL) != 0) writer.print("final ");
    if ((field.access & ACC_STATIC) != 0) writer.print("static ");

    if ((field.access & ACC_VOLATILE) != 0) writer.print("volatile ");
    if ((field.access & ACC_TRANSIENT) != 0) writer.print("transient ");

    writer.print(Type.getType(field.desc).getClassName());
    writer.print(' ');
    writer.print(field.name);

    if (field.value != null) {
      writer.print(" = ");
      writer.print(field.value);
    }

    writer.print("<br>\n");
    writer.print("<br>\n");

    // field node fields
    writer.print("<table class=\"bordered\">\n");

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Flags\n");
    writer.printf("<td class=\"bordered\">0x%x", field.access);
    if ((field.access & ACC_PUBLIC) != 0) writer.print(" ACC_PUBLIC");
    if ((field.access & ACC_PRIVATE) != 0) writer.print(" ACC_PRIVATE");
    if ((field.access & ACC_PROTECTED) != 0) writer.print(" ACC_PROTECTED");
    if ((field.access & ACC_STATIC) != 0) writer.print(" ACC_STATIC");
    if ((field.access & ACC_FINAL) != 0) writer.print(" ACC_FINAL");
    if ((field.access & ACC_VOLATILE) != 0) writer.print(" ACC_VOLATILE");
    if ((field.access & ACC_TRANSIENT) != 0) writer.print(" ACC_TRANSIENT");
    if ((field.access & ACC_SYNTHETIC) != 0) writer.print(" ACC_SYNTHETIC");
    if ((field.access & ACC_ENUM) != 0) writer.print(" ACC_ENUM");
    if ((field.access & ACC_DEPRECATED) != 0) writer.print(" ACC_DEPRECATED");
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Name\n");
    writer.print("<td class=\"bordered\">");
    writer.print(field.name);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Desc\n");
    writer.print("<td class=\"bordered\">");
    writer.print(field.desc);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Signature\n");
    writer.print("<td class=\"bordered\">");
    writer.print(field.signature);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Value\n");
    writer.print("<td class=\"bordered\">");
    writer.print(field.value);
    writer.print('\n');

    writer.print("</table>\n");
    // TODO annotations
  }

  private void print(ClassNode classNode) {
    // HTML header
    writer.print("<!DOCTYPE html>\n");
    writer.print("<html lang=\"en\">\n");
    writer.print("<meta charset=\"utf-8\"/>\n");
    writer.printf("<title>%s</title>\n", simple(classNode.name));
    writer.print("<style>\n");
    writer.print("html * {\n");
    writer.print("font-family: \"Courier New\";\n");
    writer.print("}\n");
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
    writer.print("<ul>\n");
    // TODO actually print these
    if (Etc.some(classNode.visibleAnnotations))
      writer.print("<li><a href=\"#Visible annotations\">Visible annotations</a>\n");
    if (Etc.some(classNode.invisibleAnnotations))
      writer.print("<li><a href=\"#Invisible annotations\">Invisible annotations</a>\n");
    if (Etc.some(classNode.visibleTypeAnnotations))
      writer.print("<li><a href=\"#Visible type annotations\">Visible type annotations</a>\n");
    if (Etc.some(classNode.invisibleTypeAnnotations))
      writer.print("<li><a href=\"#Invisible type annotations\">Invisible type annotations</a>\n");
    if (Etc.some(classNode.attrs)) writer.print("<li><a href=\"#Attrs\">Attrs</a>\n");
    if (Etc.some(classNode.innerClasses))
      writer.print("<li><a href=\"#Inner classes\">Inner classes</a>\n");
    writer.print("</ul>\n");
    if (Etc.some(classNode.fields)) {
      writer.print("<li><a href=\"#Fields\">Fields</a>\n");
      writer.print("<ul>\n");
      for (var field : classNode.fields) {
        var name = field.name;
        writer.printf("<li><a href=\"#%s\">%s</a>\n", name, name);
      }
      writer.print("</ul>\n");
    }
    if (Etc.some(classNode.methods)) {
      writer.print("<li><a href=\"#Methods\">Methods</a>\n");
      writer.print("<ul>\n");
      for (var method : classNode.methods) {
        var name = esc(method.name);
        writer.printf("<li><a href=\"#%s\">%s</a>\n", name, name);
      }
      writer.print("</ul>\n");
    }
    writer.print("</ul>\n");

    // class header
    writer.print("<h1 id=\"Class header\">Class header</h1>\n");

    // embellished name
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

    if (Etc.some(classNode.interfaces)) {
      writer.print(" implements");
      var more = false;
      for (var s : classNode.interfaces) {
        if (more) writer.print(',');
        more = true;
        writer.print(' ');
        writer.print(s);
      }
    }

    writer.print("<br>\n");
    writer.print("<br>\n");

    // class node fields
    writer.print("<table class=\"bordered\">\n");

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Version\n");
    writer.print("<td class=\"bordered\">");
    writer.print(classNode.version);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Flags\n");
    writer.printf("<td class=\"bordered\">0x%x", classNode.access);
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
    if ((classNode.access & ACC_DEPRECATED) != 0) writer.print(" ACC_DEPRECATED");
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Name\n");
    writer.print("<td class=\"bordered\">");
    writer.print(classNode.name);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Signature\n");
    writer.print("<td class=\"bordered\">");
    writer.print(classNode.signature);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Super\n");
    writer.print("<td class=\"bordered\">");
    writer.print(classNode.superName);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Source file\n");
    writer.print("<td class=\"bordered\">");
    writer.print(classNode.sourceFile);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Source debug\n");
    writer.print("<td class=\"bordered\">");
    writer.print(classNode.sourceDebug);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Outer class\n");
    writer.print("<td class=\"bordered\">");
    writer.print(classNode.outerClass);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Outer method\n");
    writer.print("<td class=\"bordered\">");
    writer.print(classNode.outerMethod);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Outer method desc\n");
    writer.print("<td class=\"bordered\">");
    writer.print(classNode.outerMethodDesc);
    writer.print('\n');

    writer.print("</table>\n");

    // fields
    if (Etc.some(classNode.fields)) writer.print("<h1 id=\"Fields\">Fields</h1>\n");
    for (var field : classNode.fields) print(field);

    // methods
    if (Etc.some(classNode.methods)) writer.print("<h1 id=\"Methods\">Methods</h1>\n");
    for (var method : classNode.methods) print(method);
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
