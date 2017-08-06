# gradle-stutter

[![Bintray](https://api.bintray.com/packages/ajoberstar/maven/gradle-stutter/images/download.svg)](https://bintray.com/ajoberstar/maven/gradle-stutter/_latestVersion)
[![Travis](https://img.shields.io/travis/ajoberstar/gradle-stutter.svg?style=flat-square)](https://travis-ci.org/ajoberstar/gradle-stutter)
[![Quality Gate](https://sonarqube.ajoberstar.com/api/badges/gate?key=org.ajoberstar:gradle-stutter)](https://sonarqube.ajoberstar.com/dashboard/index/org.ajoberstar:gradle-stutter)
[![GitHub license](https://img.shields.io/github/license/ajoberstar/gradle-stutter.svg?style=flat-square)](https://github.com/ajoberstar/gradle-stutter/blob/master/LICENSE)

## Why do you care?

When writing a Gradle plugin you often want to run the same suite of tests against all versions you support. The Gradle TestKit gives you the tools to do this, but makes you write the scaffolding for which versions and which tests.

## What is it?

`gradle-stutter` is a [Gradle](http://gradle.org) plugin, `org.ajoberstar.stutter`, which does some common setup for testing Gradle plugins against multiple Gradle versions.

See [java-gradle-plugin](https://docs.gradle.org/current/userguide/javaGradle_plugin.html) docs for more details on the out-of-the-box functionality.

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
    supports '3.0', '3.1', '3.2', '3.2.1' // set which Gradle versions you want to test against
}
```

### Tests

The plugin adds a `compatTest` source set that is configured via `java-gradle-plugin` as the source set for your plugin tests. This means you can leverage `withPluginClasspath` on the `GradleRunner` for your tests. The test kit dependency is **not** added by this plugin.

The following tasks are available:

- One `compatTest<version>` task per supported version
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
