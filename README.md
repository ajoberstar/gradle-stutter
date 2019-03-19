# gradle-stutter

[![Download](https://api.bintray.com/packages/ajoberstar/maven/gradle-stutter/images/download.svg)](https://bintray.com/ajoberstar/maven/gradle-stutter/_latestVersion)
[![CircleCI](https://circleci.com/gh/ajoberstar/gradle-stutter.svg?style=svg)](https://circleci.com/gh/ajoberstar/gradle-stutter)

## Why do you care?

When writing a Gradle plugin you often want to run the same suite of tests against all versions you support. The Gradle TestKit gives you the tools to do this, but makes you write the scaffolding for which versions and which tests.

## What is it?

`gradle-stutter` is a [Gradle](http://gradle.org) plugin plugin, `org.ajoberstar.stutter`, which does some common setup for testing Gradle plugins against multiple Gradle versions.

- Extension for specifying Gradle versions that are compatible with your plugin
- Allows specifying different compatible versions for each major Java version
- Task to create lock file listing compatible Gradle versions
- Generates a compatibility test task for your suite for each locked version applicable to the JVM Gradle is running under

See [java-gradle-plugin](https://docs.gradle.org/current/userguide/javaGradle_plugin.html) docs for more details on Gradle's out-of-the-box functionality.

## Usage

See the [Release Notes](https://github.com/ajoberstar/gradle-stutter/releases) for updates on
changes and compatibility with Java and Gradle versions.

### Applying the Plugin

**Plugins DSL**

```groovy
plugins {
    id 'org.ajoberstar.stutter' version '<version>'
}
```

**Classic**

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'org.ajoberstar:gradle-stutter:<version>'
    }
}

apply plugin: 'org.ajoberstar.stutter'
```

### Configuration

```groovy
stutter {
  // Only match min/max within that otherwise matches your compatibility specs in each Gradle major version
  sparse = true // defaults to false

  // specify compatible Gradle versions for Java 8+
  java(8) {
    compatibleRange '3.0', '4.0' // include 3.0 <= version < 4.0
    compatibleRange '4.2' // include 4.2 <= version
    compatible '2.14', '1.2' // include 2.14 and 1.12 specifically
    incompatible '3.3' // exclude 3.3 even if included above
  }

  // specify compatible Gradle versions for Java 9+
  java(9) {
    compatibleRange '4.0' // include 4.0 <= version
  }

  // You don't have to specify compatible Gradle versions for all Java versions you run Gradle with
  // If an exact match isn't found, Stutter will use the lock file for the latest compatible JVM
  // e.g. if you specify Java 8 and 9, as above
  //      Gradle run under Java 8 will only use versions listed in the java(8) block
  //      Gradle run under Java 9 will only use versions listed in the java(9) block
  //      Gradle run under Java 10 will only use versions listed in the java(9) block

  // If you have a lot of tests, or otherwise just don't want to test every Gradle version that you say is compatible,
  // use sparse = true. This will greatly limit the number of versions you test against, but should do the job of
  // verifying compatibility.
  // e.g.  compatible '2.14' and compatibleRange '3.0'
  //       matches '2.14', '3.0', '3.5.1', '4.0', '4.7' (presuming 4.7 is the latest available 4.x)
}
```

### Lock file

Lock files will be generated/used from the `<project>/.stutter` directory.

During Gradle's configuration phase, Stutter will look for the latest compatible lock file for the JVM you're running under. (e.g. Under Java 10, it will look for `java10.lock`, then `java9.lock`, then `java8.lock`, etc. until it finds a lock file)

Lock files are generated with the `stutterWriteLocks` task based on the configuration of the `stutter` extension (see above).

```
./gradlew stutterWriteLocks
```

### Tests

The plugin adds a `compatTest` source set that is configured via `java-gradle-plugin` as the source set for your plugin tests. This means you can leverage `withPluginClasspath` on the `GradleRunner` for your tests. The test kit dependency is **not** added by this plugin.

The following tasks are available:

- One `compatTest<version>` task per supported version (based on the compatible lock file -- see above for details)
- An overall `compatTest` which depends on the version-specific ones
- `check` will depend on `compatTest`, so that these run during your normal checks

Your tests should reference the `compat.gradle.version` system property when they specify a version on the `GradleRunner`:

```java
GradleRunner.create()
    .withGradleVersion(System.getProperty("compat.gradle.version"))
    //...
```

## Questions, Bugs, and Features

Please use the repo's [issues](https://github.com/ajoberstar/gradle-stutter/issues)
for all questions, bug reports, and feature requests.

## Contributing

Contributions are very welcome and are accepted through pull requests.

Smaller changes can come directly as a PR, but larger or more complex
ones should be discussed in an issue first to flesh out the approach.

If you're interested in implementing a feature on the
[issues backlog](https://github.com/ajoberstar/gradle-stutter/issues), add a comment
to make sure it's not already in progress and for any needed discussion.

## Acknowledgements

Thanks to all of the [contributors](https://github.com/ajoberstar/gradle-stutter/graphs/contributors).

## Alternatives

### TestKit (out-of-the-box)

[TestKit](https://docs.gradle.org/current/userguide/test_kit.html) is built into Gradle, so it should be your first consideration. It provides a good interface to kick off full test builds and verify the output/tasks that ran. This tends to be a far more effective way to test Gradle plugin code than unit testing, due to the complexity of Gradle.

On top of TestKit, Stutter provides a convenient way to run a test suite against multiple versions of Gradle. This is very useful for verifying compatibility.

### GradleTest

The [org.ysb33r.gradletest](https://github.com/ysb33r/gradleTest) plugin is optimized for creating and testing sample projects without you having to directly interact with TestKit. These can also be tested across multiple versions of Gradle providing helpful compatibility verification. GradleTest also provides the ability to test Gradle versions far older than Stutter does.

Stutter leaves the user to decide how to leverage TestKit, it just helps provide the ability to test multiple versions with the same suite. If your use case is more centered around samples or full project tests, GradleTest may be a better fit.
