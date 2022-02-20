package org.ajoberstar.gradle.stutter;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.provider.Property;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JavaToolchainSpec;

public abstract class StutterMatrix implements Named {
  private final StutterGradleVersions gradleVersions = new StutterGradleVersions();

  @Inject
  protected abstract JavaToolchainService getToolchainService();

  public abstract Property<JavaLauncher> getJavaLauncher();

  public void javaToolchain(Action<? super JavaToolchainSpec> configureAction) {
    getJavaLauncher().set(getToolchainService().launcherFor(configureAction));
  }

  public StutterGradleVersions getGradleVersions() {
    return gradleVersions;
  }

  public void gradleVersions(Action<? super StutterGradleVersions> action) {
    action.execute(gradleVersions);
  }
}
