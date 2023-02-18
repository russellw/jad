package jad;

import java.io.IOException;

public class Main {
  private static final Option[] OPTIONS = new Option[] {};

  public static void main(String[] args) throws IOException {
    Option.parse(OPTIONS, args);
  }
}
