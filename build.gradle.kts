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
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

dependencies {
  implementation(platform("com.fasterxml.jackson:jackson-bom:[2.19,2.20)"))
  implementation("com.fasterxml.jackson.core:jackson-databind")
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter("[5.0,6.0)")
    }

    val compatTest by getting(JvmTestSuite::class) {
      useSpock("2.3-groovy-4.0")

      dependencies {
        implementation(gradleTestKit())
      }

      // TEMP until beta.2 disable the base target
      targets {
        val compatTest by getting {
          testTask {
            enabled = false
          }
        }
      }
    }
  }
}

tasks.named<Jar>("jar") {
  manifest {
    attributes.put("Automatic-Module-Name", "org.ajoberstar.stutter")
  }
}

stutter {
  val java17 by matrices.creating {
    javaToolchain {
      languageVersion.set(JavaLanguageVersion.of(17))
    }
    gradleVersions {
      compatibleRange("9.0")
    }
  }

  val java21 by matrices.creating {
    javaToolchain {
      languageVersion.set(JavaLanguageVersion.of(21))
    }
    gradleVersions {
      compatibleRange("9.0")
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
