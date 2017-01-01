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

        project.getPluginManager().withPlugin("java", plugin -> {
            JavaPluginConvention java = project.getConvention().getPlugin(JavaPluginConvention.class);
            SourceSet sourceSet = java.getSourceSets().create("compatTest");

            Task root = project.getTasks().create("compatTest");
            root.setGroup("verification");
            root.setDescription("Run compatibility tests against all supported Gradle versions.");
            project.getTasks().getByName("check").dependsOn(root);

            stutter.setAction(gradleVersion -> {
                Test task = project.getTasks().create("compatTest" + gradleVersion, Test.class);
                task.setGroup("verification");
                task.setDescription("Run compatibility tests against Gradle " + gradleVersion);
                task.setTestClassesDir(sourceSet.getOutput().getClassesDir());
                task.setClasspath(sourceSet.getRuntimeClasspath());
                task.systemProperty("compat.gradle.version", gradleVersion);
                root.dependsOn(task);
            });

            project.getPluginManager().withPlugin("java-gradle-plugin", plugin2 -> {
                GradlePluginDevelopmentExtension gradlePlugin = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
                gradlePlugin.testSourceSets(sourceSet);
            });
        });
    }
}
