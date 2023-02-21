package jad;

import static org.objectweb.asm.Opcodes.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public final class HtmlPrinter {
  private Set<String> classNames = new HashSet<>();
  private PrintWriter writer;

  private void print(MethodNode methodNode) {
    // heading
    var name = esc(methodNode.name);
    writer.printf("<h2 id=\"%s\">%s</h2>\n", name, name);

    // embellished name
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

    if (Etc.some(methodNode.exceptions)) {
      writer.print(" throws");
      more = false;
      for (var s : methodNode.exceptions) {
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
    writer.printf("<td class=\"bordered\">0x%x", methodNode.access);
    if ((methodNode.access & ACC_PUBLIC) != 0) writer.print(" ACC_PUBLIC");
    if ((methodNode.access & ACC_PRIVATE) != 0) writer.print(" ACC_PRIVATE");
    if ((methodNode.access & ACC_PROTECTED) != 0) writer.print(" ACC_PROTECTED");
    if ((methodNode.access & ACC_STATIC) != 0) writer.print(" ACC_STATIC");
    if ((methodNode.access & ACC_FINAL) != 0) writer.print(" ACC_FINAL");
    if ((methodNode.access & ACC_SYNCHRONIZED) != 0) writer.print(" ACC_SYNCHRONIZED");
    if ((methodNode.access & ACC_BRIDGE) != 0) writer.print(" ACC_BRIDGE");
    if ((methodNode.access & ACC_VARARGS) != 0) writer.print(" ACC_VARARGS");
    if ((methodNode.access & ACC_NATIVE) != 0) writer.print(" ACC_NATIVE");
    if ((methodNode.access & ACC_ABSTRACT) != 0) writer.print(" ACC_ABSTRACT");
    if ((methodNode.access & ACC_STRICT) != 0) writer.print(" ACC_STRICT");
    if ((methodNode.access & ACC_SYNTHETIC) != 0) writer.print(" ACC_SYNTHETIC");
    if ((methodNode.access & ACC_DEPRECATED) != 0) writer.print(" ACC_DEPRECATED");
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Name\n");
    writer.print("<td class=\"bordered\">");
    writer.print(methodNode.name);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Desc\n");
    writer.print("<td class=\"bordered\">");
    writer.print(methodNode.desc);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Signature\n");
    writer.print("<td class=\"bordered\">");
    writer.print(methodNode.signature);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Annotation default\n");
    writer.print("<td class=\"bordered\">");
    writer.print(methodNode.annotationDefault);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Max stack\n");
    writer.print("<td class=\"bordered\">");
    writer.print(methodNode.maxStack);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Max locals\n");
    writer.print("<td class=\"bordered\">");
    writer.print(methodNode.maxLocals);
    writer.print('\n');

    writer.print("</table>\n");
    writer.print("<br>\n");

    // TODO parameters and annotations

    // instructions
    var i = 0;
    var labels = new HashMap<LabelNode, String>();
    for (var abstractInsnNode : methodNode.instructions)
      if (abstractInsnNode instanceof LabelNode labelNode) labels.put(labelNode, "L" + i++);

    writer.print("<table>\n");

    writer.print("<tr>\n");
    writer.print("<th>Line\n");
    writer.print("<th>Label\n");
    writer.print("<th>Opcode\n");
    writer.print("<th>Operands\n");

    var line = -1;
    String label = null;
    FrameNode frameNode = null;
    for (var abstractInsnNode : methodNode.instructions) {
      if (abstractInsnNode instanceof LineNumberNode lineNumberNode) {
        assert line < 0;
        line = lineNumberNode.line;
        continue;
      }
      if (abstractInsnNode instanceof LabelNode labelNode) {
        assert label == null;
        label = labels.get(labelNode);
        continue;
      }
      if (abstractInsnNode instanceof FrameNode frameNode1) {
        // assert frameNode == null;
        frameNode = frameNode1;
        continue;
      }

      writer.print("<tr>\n");

      // line number
      writer.print("<td>");
      if (line >= 0) {
        writer.print(line);
        line = -1;
      }
      writer.print('\n');

      // label
      writer.print("<td>");
      if (label != null) {
        writer.print(label);
        label = null;
      }
      writer.print('\n');

      // opcode
      writer.print("<td>");
      var opcode = abstractInsnNode.getOpcode();
      if (opcode >= 0) writer.print(Etc.mnemonics[opcode]);

      // operands
      switch (abstractInsnNode.getType()) {
        case AbstractInsnNode.INSN -> {}
        case AbstractInsnNode.INT_INSN -> {
          var a = (IntInsnNode) abstractInsnNode;
          writer.print("<td>");
          writer.print(a.operand);
        }
        case AbstractInsnNode.VAR_INSN -> {
          var a = (VarInsnNode) abstractInsnNode;
          writer.print("<td>%");
          writer.print(a.var);
        }
        case AbstractInsnNode.TYPE_INSN -> {
          var a = (TypeInsnNode) abstractInsnNode;
          writer.print("<td>");
          writer.print(a.desc);
        }
        case AbstractInsnNode.FIELD_INSN -> {
          var a = (FieldInsnNode) abstractInsnNode;
          writer.print("<td>");
          writer.print(a.name);
          writer.print(' ');
          writer.print(a.getType());
        }
        case AbstractInsnNode.METHOD_INSN -> {
          var a = (MethodInsnNode) abstractInsnNode;
          writer.print("<td>");
          writer.print(a.name);
          writer.print(' ');
          writer.print(a.getType());
        }
        case AbstractInsnNode.JUMP_INSN -> {
          var a = (JumpInsnNode) abstractInsnNode;
          writer.print("<td>");
          writer.print(labels.get(a.label));
        }
        case AbstractInsnNode.LDC_INSN -> {
          var a = (LdcInsnNode) abstractInsnNode;
          writer.print("<td>");
          writer.print(a.cst);
        }
        case AbstractInsnNode.IINC_INSN -> {
          var a = (IincInsnNode) abstractInsnNode;
          writer.print("<td>%");
          writer.print(a.var);
          writer.print(' ');
          writer.print(a.incr);
        }
        case AbstractInsnNode.TABLESWITCH_INSN -> {
          var a = (TableSwitchInsnNode) abstractInsnNode;
          writer.print("<td>");
          writer.print(a.min);
          writer.print(' ');
          writer.print(a.max);
          writer.print(' ');
          writer.print(a.labels);
        }
        case AbstractInsnNode.LOOKUPSWITCH_INSN -> {
          var a = (LookupSwitchInsnNode) abstractInsnNode;
          writer.print("<td>");
          writer.print(a.keys);
          writer.print(' ');
          writer.print(a.labels);
        }
        default -> throw new IllegalArgumentException(Integer.toString(abstractInsnNode.getType()));
      }

      writer.print('\n');
    }

    writer.print("</table>\n");
  }

  private void print(FieldNode fieldNode) {
    // heading
    var name = fieldNode.name;
    writer.printf("<h2 id=\"%s\">%s</h2>\n", name, name);

    // embellished name
    if ((fieldNode.access & ACC_PUBLIC) != 0) writer.print("public ");
    if ((fieldNode.access & ACC_PRIVATE) != 0) writer.print("private ");
    if ((fieldNode.access & ACC_PROTECTED) != 0) writer.print("protected ");

    if ((fieldNode.access & ACC_FINAL) != 0) writer.print("final ");
    if ((fieldNode.access & ACC_STATIC) != 0) writer.print("static ");

    if ((fieldNode.access & ACC_VOLATILE) != 0) writer.print("volatile ");
    if ((fieldNode.access & ACC_TRANSIENT) != 0) writer.print("transient ");

    writer.print(Type.getType(fieldNode.desc).getClassName());
    writer.print(' ');
    writer.print(fieldNode.name);

    if (fieldNode.value != null) {
      writer.print(" = ");
      writer.print(fieldNode.value);
    }

    writer.print("<br>\n");
    writer.print("<br>\n");

    // field node fields
    writer.print("<table class=\"bordered\">\n");

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Flags\n");
    writer.printf("<td class=\"bordered\">0x%x", fieldNode.access);
    if ((fieldNode.access & ACC_PUBLIC) != 0) writer.print(" ACC_PUBLIC");
    if ((fieldNode.access & ACC_PRIVATE) != 0) writer.print(" ACC_PRIVATE");
    if ((fieldNode.access & ACC_PROTECTED) != 0) writer.print(" ACC_PROTECTED");
    if ((fieldNode.access & ACC_STATIC) != 0) writer.print(" ACC_STATIC");
    if ((fieldNode.access & ACC_FINAL) != 0) writer.print(" ACC_FINAL");
    if ((fieldNode.access & ACC_VOLATILE) != 0) writer.print(" ACC_VOLATILE");
    if ((fieldNode.access & ACC_TRANSIENT) != 0) writer.print(" ACC_TRANSIENT");
    if ((fieldNode.access & ACC_SYNTHETIC) != 0) writer.print(" ACC_SYNTHETIC");
    if ((fieldNode.access & ACC_ENUM) != 0) writer.print(" ACC_ENUM");
    if ((fieldNode.access & ACC_DEPRECATED) != 0) writer.print(" ACC_DEPRECATED");
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Name\n");
    writer.print("<td class=\"bordered\">");
    writer.print(fieldNode.name);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Desc\n");
    writer.print("<td class=\"bordered\">");
    writer.print(fieldNode.desc);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Signature\n");
    writer.print("<td class=\"bordered\">");
    writer.print(fieldNode.signature);
    writer.print('\n');

    writer.print("<tr>\n");
    writer.print("<td class=\"bordered\">Value\n");
    writer.print("<td class=\"bordered\">");
    writer.print(fieldNode.value);
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
    writer.print("table.bordered, th.bordered, td.bordered {\n");
    writer.print("border: 1px solid;\n");
    writer.print("border-collapse: collapse;\n");
    writer.print("}\n");
    writer.print("th {\n");
    writer.print("padding-left: 5px;\n");
    writer.print("padding-right: 5px;\n");
    writer.print("}\n");
    writer.print("td {\n");
    writer.print("padding-left: 5px;\n");
    writer.print("padding-right: 5px;\n");
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
      for (var fieldNode : classNode.fields) {
        var name = fieldNode.name;
        writer.printf("<li><a href=\"#%s\">%s</a>\n", name, name);
      }
      writer.print("</ul>\n");
    }
    if (Etc.some(classNode.methods)) {
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
    for (var fieldNode : classNode.fields) print(fieldNode);

    // methods
    if (Etc.some(classNode.methods)) writer.print("<h1 id=\"Methods\">Methods</h1>\n");
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
