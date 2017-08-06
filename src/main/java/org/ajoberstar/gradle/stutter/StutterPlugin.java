/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ajoberstar.gradle.stutter;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.Task;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.plugins.JavaPluginConvention;

public class StutterPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    StutterExtension stutter = project.getExtensions().create("stutter", StutterExtension.class);

    project
        .getPluginManager()
        .withPlugin(
            "java",
            plugin -> {
              JavaPluginConvention java =
                  project.getConvention().getPlugin(JavaPluginConvention.class);
              SourceSet sourceSet = java.getSourceSets().create("compatTest");

              Task root = project.getTasks().create("compatTest");
              root.setGroup("verification");
              root.setDescription("Run compatibility tests against all supported Gradle versions.");
              project.getTasks().getByName("check").dependsOn(root);

              stutter.setAction(
                  gradleVersion -> {
                    Test task = project.getTasks().create("compatTest" + gradleVersion, Test.class);
                    task.setGroup("verification");
                    task.setDescription("Run compatibility tests against Gradle " + gradleVersion);
                    task.setTestClassesDirs(sourceSet.getOutput().getClassesDirs());
                    task.setClasspath(sourceSet.getRuntimeClasspath());
                    task.systemProperty("compat.gradle.version", gradleVersion);
                    root.dependsOn(task);
                  });

              project
                  .getPluginManager()
                  .withPlugin(
                      "java-gradle-plugin",
                      plugin2 -> {
                        GradlePluginDevelopmentExtension gradlePlugin =
                            project
                                .getExtensions()
                                .getByType(GradlePluginDevelopmentExtension.class);
                        gradlePlugin.testSourceSets(sourceSet);
                      });
            });
  }
}
