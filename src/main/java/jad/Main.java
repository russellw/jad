package jad;

import static org.objectweb.asm.Opcodes.ASM9;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class Main {
  private static final Option[] OPTIONS = new Option[] {};

  public static void main(String[] args) throws IOException {
    Option.parse(OPTIONS, args);
    var classes = new ArrayList<ClassNode>();
    for (var file : Option.positionalArgs) {
      var classReader = new ClassReader(Files.readAllBytes(Path.of(file)));
      var classNode = new ClassNode(ASM9);
      classReader.accept(classNode, 0);
      classes.add(classNode);
    }
    new HtmlPrinter(classes);
  }
}
