package org.ajoberstar.gradle.stutter;

import java.util.Arrays;
import java.util.function.Consumer;

public class StutterExtension {
  private Consumer<String> action;

  public void supports(String... versions) {
    Arrays.stream(versions).forEach(action);
  }

  void setAction(Consumer<String> action) {
    this.action = action;
  }
}
