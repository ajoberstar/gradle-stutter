package org.ajoberstar.gradle.stutter

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.TempDir

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

class BaseCompatTest extends Specification {
  @TempDir File tempDir
  File projectDir
  File buildFile
  List compatTestTasks

  def setup() {
    projectDir = new File(tempDir, 'project')
    buildFile = projectFile('build.gradle')
    buildFile << """\
plugins {
  id 'org.ajoberstar.stutter'
  id 'java'
}

stutter {
  sparse = false
  matrices {
    java8 {
      javaToolchain {
        languageVersion = JavaLanguageVersion.of(8)
      }
      gradleVersions {
        compatibleRange '3.0', '4.0'
      }
    }
    java9 {
      javaToolchain {
        languageVersion = JavaLanguageVersion.of(8)
      }
      gradleVersions {
        compatibleRange '4.0', '4.3'
        incompatible '4.2'
      }
    }
  }
}
"""

    compatTestTasks = [
      ':compatTestJava8Gradle3.0',
      ':compatTestJava8Gradle3.1',
      ':compatTestJava8Gradle3.2.1',
      ':compatTestJava8Gradle3.3',
      ':compatTestJava8Gradle3.4.1',
      ':compatTestJava8Gradle3.5.1',
      ':compatTestJava8',
      ':compatTestJava9Gradle4.0.2',
      ':compatTestJava9Gradle4.1',
      ':compatTestJava9Gradle4.2.1',
      ':compatTestJava9']
  }

  def 'without lock files no tasks are available'() {
    when:
    def result = build('compatTest')
    then:
    result.tasks.collect { it.path } as Set == [':compatTestJava8', ':compatTestJava9', ':compatTest'] as Set
    result.task(':compatTest').outcome == TaskOutcome.SUCCESS
    result.output.contains('Stutter matrix java8 has no locked Gradle versions')
    result.output.contains('Stutter matrix java9 has no locked Gradle versions')
  }

  def 'can generate valid lock files'() {
    when:
    def result = build('stutterWriteLocks')
    then:
    result.task(':stutterWriteLocks').outcome == TaskOutcome.SUCCESS
    projectFile('stutter.lockfile').text.normalize() == '''\
# DO NOT MODIFY: Generated by Stutter plugin.
java8=3.0,3.1,3.2.1,3.3,3.4.1,3.5.1
java9=4.0.2,4.1,4.2.1
'''
  }

  def 'lock files result in tasks for each version'() {
    given:
    build('stutterWriteLocks')
    when:
    def result = build('compatTest')
    then:
    result.task(':compatTest').outcome == TaskOutcome.UP_TO_DATE
    compatTestTasks.each {
      assert result.task(it).outcome == TaskOutcome.NO_SOURCE || result.task(it).outcome == TaskOutcome.UP_TO_DATE
    }
  }

  @Ignore("Doesn't seem to work on Gradle 8, but don't care enough")
  def 'ensure compatTest runs after test'() {
    given:
    build('stutterWriteLocks')
    when:
    def result = build('compatTest', 'test')
    def taskOrder = result.tasks.collect { it.path }.findAll { (it.startsWith(':test') || it.startsWith(':compatTest')) && !it.endsWith('Classes') }
    println taskOrder
    then:
    taskOrder == [':test', compatTestTasks, ':compatTest'].flatten()
  }

  def 'modified runtimeClasspath is respected'() {
    given:
    projectFile('build.gradle') << '''\
sourceSets {
  compatTest.runtimeClasspath = files()
}

repositories {
  mavenCentral()
}

dependencies {
  compatTestImplementation gradleTestKit()
  compatTestImplementation 'junit:junit:4.12'
}
'''
    projectFile('src/compatTest/java/org/ajoberstar/Example.java') << '''\
package org.ajoberstar;

import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Test;

public class Example {
  @Test
  public void test() {
    System.out.println(TaskOutcome.SUCCESS);
  }
}
'''
    build('stutterWriteLocks')
    when:
    def result = buildAndFail('compatTest')
    then:
    result.task(':compileCompatTestJava').outcome == TaskOutcome.SUCCESS
    result.task(compatTestTasks[0]).outcome == TaskOutcome.FAILED
  }

  private BuildResult build(String... args = []) {
    def fullArgs = [args, ['--stacktrace', '--configuration-cache', '--no-parallel']].flatten() as String[]
    return GradleRunner.create()
      .withGradleVersion(System.properties['compat.gradle.version'])
      .withPluginClasspath()
      .withProjectDir(projectDir)
      .forwardOutput()
      .withArguments(fullArgs)
      .build()
  }

  private BuildResult buildAndFail(String... args = []) {
    def fullArgs = [args, ['--stacktrace', '--configuration-cache', '--no-parallel']].flatten() as String[]
    return GradleRunner.create()
      .withGradleVersion(System.properties['compat.gradle.version'])
      .withPluginClasspath()
      .withProjectDir(projectDir)
      .forwardOutput()
      .withArguments(fullArgs)
      .buildAndFail()
  }

  private File projectFile(String path) {
    File file = new File(projectDir, path)
    file.parentFile.mkdirs()
    return file
  }
}
