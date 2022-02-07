package org.ajoberstar.gradle.stutter;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.jvm.toolchain.JavaToolchainSpec;

public class StutterMatrix implements Named {
  private final String name;
  private Action<? super JavaToolchainSpec> javaToolchainSpec;
  private StutterGradleVersions gradleVersions;

  @Inject
  public StutterMatrix(String name) {
    this.name = name;
    this.javaToolchainSpec = spec -> {
      throw new IllegalStateException("Java toolchain spec not configured for matrix: " + name);
    };
    this.gradleVersions = new StutterGradleVersions();
  }

  @Override
  public String getName() {
    return name;
  }

  Action<? super JavaToolchainSpec> getJavaToolchainSpec() {
    return javaToolchainSpec;
  }

  public void javaToolchain(Action<? super JavaToolchainSpec> spec) {
    this.javaToolchainSpec = spec;
  }

  public StutterGradleVersions getGradleVersions() {
    return gradleVersions;
  }

  public void gradleVersions(Action<? super StutterGradleVersions> configureAction) {
    configureAction.execute(gradleVersions);
  }
}
