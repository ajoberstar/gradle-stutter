package org.ajoberstar.gradle.stutter;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.plugins.jvm.JvmTestSuiteTarget;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.testing.Test;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.testing.base.TestingExtension;
import org.gradle.util.GradleVersion;

public class StutterPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    var stutter = project.getExtensions().create("stutter", StutterExtension.class);
    stutter.getLockFile().convention(project.getLayout().getProjectDirectory().file("stutter.lockfile"));
    stutter.getSparse().convention(true);

    project.getPluginManager().withPlugin("java", plugin -> {
      createWriteLocksTask(project, stutter);
      configureCompatTest(project, stutter);
    });
  }

  private void createWriteLocksTask(Project project, StutterExtension stutter) {
    project.getTasks().register("stutterWriteLocks", GenerateStutterLocks.class, task -> {
      task.setGroup("plugin development");
      task.setDescription("Generate lock files of Gradle versions to test for compatibility.");
      task.getOutputFile().set(stutter.getLockFile());
      task.getMatrices().set(stutter.getMatrices());
      task.getSparse().set(stutter.getSparse());
    });
  }

  private void configureCompatTest(Project project, StutterExtension stutter) {
    var testing = project.getExtensions().getByType(TestingExtension.class);
    testing.getSuites().register("compatTest", JvmTestSuite.class, suite -> {
      createCompatTestTasks(project, stutter, suite);
    });
  }

  private void createCompatTestTasks(Project project, StutterExtension stutter, JvmTestSuite suite) {
    var lockedVersions = getLockedVersions(project, stutter);

    var test = project.getTasks().named("test");

    var root = project.getTasks().named("compatTest", Test.class, task -> {
      task.setGroup("verification");
      task.setDescription("Run compatibility tests against all supported Gradle and Java versions.");
      task.setScanForTestClasses(false);
    });

    stutter.getMatrices().all(matrix -> {
      var capitalizedMatrixName = matrix.getName().substring(0, 1).toUpperCase() + matrix.getName().substring(1);
      var matrixTaskName = "compatTest" + capitalizedMatrixName;
      var matrixRoot = project.getTasks().register(matrixTaskName, task -> {
        task.setGroup("verification");
        task.setDescription(String.format("Run compatibility tests against all %s supported Gradle versions.", matrix.getName()));
      });

      root.configure(rootTask -> rootTask.dependsOn(matrixRoot));

      var matrixLockedVersions = lockedVersions.getOrDefault(matrix.getName(), Set.of());

      if (matrixLockedVersions.isEmpty()) {
        matrixRoot.configure(task -> {
          task.doFirst(t -> {
            task.getLogger().warn("Stutter matrix {} has no locked Gradle versions. Configure the stutter extension and run stutterWriteLocks.", matrix.getName());
          });
        });
      }

      matrixLockedVersions.forEach(gradleVersion -> {
        var taskName = String.format("compatTest%sGradle%s", capitalizedMatrixName, gradleVersion.getVersion());

        var versionTarget = suite.getTargets().register(taskName, target -> {
          target.getTestTask().configure(task -> {
            task.setGroup("verification");
            task.setDescription(String.format("Run compatibility tests for %s against Gradle %s", matrix.getName(), gradleVersion.getVersion()));

            task.getJavaLauncher().set(matrix.getJavaLauncher());
            task.systemProperty("compat.gradle.version", gradleVersion.getVersion());

            task.shouldRunAfter(test);
          });
        });

        matrixRoot.configure(rootTask -> rootTask.dependsOn(versionTarget.map(JvmTestSuiteTarget::getTestTask)));
      });
    });

    project.getPluginManager().withPlugin("java-gradle-plugin", plugin2 -> {
      GradlePluginDevelopmentExtension gradlePlugin = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
      gradlePlugin.testSourceSets(suite.getSources());
    });
  }

  private Map<String, Set<GradleVersion>> getLockedVersions(Project project, StutterExtension stutter) {
    var lockFile = stutter.getLockFile()
        .get()
        .getAsFile();

    if (!lockFile.exists()) {
      project.getLogger().warn("No Stutter lockfile found at: {}", lockFile);
      // lock file doesn't exist, so let's get out of here
      return Map.of();
    }

    var lockFileBytes = project.getProviders().fileContents(stutter.getLockFile())
        .getAsBytes();

    try (var inputStream = new ByteArrayInputStream(lockFileBytes.get());
        var reader = new BufferedReader(new InputStreamReader(inputStream))) {
      return reader.lines()
          // skip lines starting with #
          .filter(line -> !line.startsWith("#"))
          .map(line -> {
            var parts = line.split("=", 2);
            var matrixName = parts[0];
            var gradleVersions = Arrays.stream(parts[1].split(","))
                .map(GradleVersion::version)
                .collect(Collectors.toSet());
            return Map.entry(matrixName, gradleVersions);
          })
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
