package org.ajoberstar.gradle.stutter;

import org.gradle.api.Project;
import org.gradle.api.Plugin;

public class StutterPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        StutterExtension stutter = project.getExtensions().create("stutter", StutterExtension.class);

        project.getPluginManager().withPlugin("java", plugin -> {
            JavaPluginConvention java = project.getExtensions().getByType(JavaPluginConvention.class);
            SourceSet sourceSet = java.getSourceSets().create("compatTest");

            Task root = project.getTasks().create("compatTest");
            project.getTasks().getByName("check").dependsOn(root);

            stutter.setAction(gradleVersion -> {
                Task task = project.getTasks().create("compatTest" + gradleVersion, Test.class);
                task.setTestClassesDir(sourceSet.getOutput().getClassesDir());
                task.setClasspath(sourceSet.getRuntimeClasspath());
                task.systemProperty("compat.gradle.version", gradleVersion);
                root.dependsOn(task);
            });

            project.getPluginManager().withPlugin("java-gradle-plugin", plugin -> {
                GradlePluginDevelopmentExtension gradlePlugin = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
                gradlePlugin.testSourceSets(sourceSet);
            });
        });
    }
}
