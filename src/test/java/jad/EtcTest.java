package jad;

import static org.junit.Assert.*;
import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

public class EtcTest {
  static String name;
  static String superName;
  static final Set<String> methods = new HashSet<>();
  static int end;

  static final class TestClassPrinter extends ClassVisitor {
    TestClassPrinter() {
      super(ASM9);
    }

    @Override
    public void visit(
        int version,
        int access,
        String name,
        String signature,
        String superName,
        String[] interfaces) {
      EtcTest.name = name;
      EtcTest.superName = superName;
    }

    @Override
    public MethodVisitor visitMethod(
        int access, String name, String descriptor, String signature, String[] exceptions) {
      methods.add(name);
      return null;
    }

    @Override
    public void visitEnd() {
      end++;
    }
  }

  @Test
  public void ext() {
    assertEquals(Etc.ext("foo.txt"), "txt");
    assertEquals(Etc.ext("foo"), "");
  }

  @Test
  public void classReader() {
    try {
      var classReader = new ClassReader("java.lang.String");
      var classPrinter = new TestClassPrinter();
      classReader.accept(classPrinter, 0);
      assertEquals(name, "java/lang/String");
      assertEquals(superName, "java/lang/Object");
      assert methods.contains("equals");
      assertEquals(end, 1);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void classNode() {
    try {
      var classReader = new ClassReader("java.lang.String");
      var classNode = new ClassNode(ASM9);
      classReader.accept(classNode, 0);

      assertTrue((classNode.access & ACC_PUBLIC) != 0);
      assertEquals(classNode.name, "java/lang/String");
      assertEquals(classNode.superName, "java/lang/Object");

      final Set<String> methods = new HashSet<>();
      for (var methodNode : classNode.methods) methods.add(methodNode.name);
      assert methods.contains("equals");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
