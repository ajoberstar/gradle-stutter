package org.ajoberstar.gradle.stutter;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JavaToolchainSpec;

public class StutterMatrix implements Named {
  private final String name;
  private final Property<JavaLauncher> javaLauncher;
  private final StutterGradleVersions gradleVersions;
  private final JavaToolchainService toolchain;

  @Inject
  public StutterMatrix(String name, ObjectFactory objectFactory, JavaToolchainService toolchain) {
    this.name = name;
    this.javaLauncher = objectFactory.property(JavaLauncher.class);
    this.gradleVersions = new StutterGradleVersions();
    this.toolchain = toolchain;
  }

  @Override
  public String getName() {
    return name;
  }

  Property<JavaLauncher> getJavaLauncher() {
    return javaLauncher;
  }

  public void javaToolchain(Action<? super JavaToolchainSpec> configureAction) {
    javaLauncher.set(toolchain.launcherFor(configureAction));
  }

  public StutterGradleVersions getGradleVersions() {
    return gradleVersions;
  }

  public void gradleVersions(Action<? super StutterGradleVersions> configureAction) {
    configureAction.execute(gradleVersions);
  }
}
