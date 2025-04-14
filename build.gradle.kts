plugins {
  id("org.ajoberstar.defaults.gradle-plugin")
  id("groovy")

  id("org.ajoberstar.stutter")
}

group = "org.ajoberstar"
description = "A Gradle plugin plugin"

mavenCentral {
  developerName.set("Andrew Oberstar")
  developerEmail.set("ajoberstar@gmail.com")
  githubOwner.set("ajoberstar")
  githubRepository.set("gradle-stutter")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}

dependencies {
  implementation(platform("com.fasterxml.jackson:jackson-bom:[2.18,2.19)"))
  implementation("com.fasterxml.jackson.core:jackson-databind")

  compatTestImplementation(gradleTestKit())
  compatTestImplementation("org.spockframework:spock-core:2.3-groovy-3.0")
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter("latest.release")
    }
  }
}

tasks.named<Jar>("jar") {
  manifest {
    attributes.put("Automatic-Module-Name", "org.ajoberstar.stutter")
  }
}

stutter {
  val java11 by matrices.creating {
    javaToolchain {
      languageVersion.set(JavaLanguageVersion.of(11))
    }
    gradleVersions {
      compatibleRange("7.4.2")
    }
  }

  val java17 by matrices.creating {
    javaToolchain {
      languageVersion.set(JavaLanguageVersion.of(17))
    }
    gradleVersions {
      compatibleRange("7.4.2")
    }
  }

  val java21 by matrices.creating {
    javaToolchain {
      languageVersion.set(JavaLanguageVersion.of(21))
    }
    gradleVersions {
      compatibleRange("8.4")
    }
  }
}

tasks.named("check") {
  dependsOn(tasks.named("compatTest"))
}

gradlePlugin {
  plugins {
    create("plugin") {
      id = "org.ajoberstar.stutter"
      displayName = "Stutter Plugin"
      description = "A Gradle plugin plugin"
      implementationClass = "org.ajoberstar.gradle.stutter.StutterPlugin"
    }
  }
}
