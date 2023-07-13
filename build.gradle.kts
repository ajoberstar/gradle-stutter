plugins {
  id("org.ajoberstar.defaults.gradle-plugin")
  id("groovy")

  id("org.ajoberstar.stutter")
}

group = "org.ajoberstar"
description = "A Gradle plugin plugin"

mavenCentral {
  developerName = "Andrew Oberstar"
  developerEmail = "ajoberstar@gmail.com"
  githubOwner = "ajoberstar"
  githubRepository = "gradle-stutter"
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(11)
  }
}

dependencies {
  implementation(platform("com.fasterxml.jackson:jackson-bom:[2.14,2.15)"))
  implementation("com.fasterxml.jackson.core:jackson-databind")

  testImplementation("org.junit.jupiter:junit-jupiter-api:latest.release")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:latest.release")

  compatTestImplementation(gradleTestKit())
  compatTestImplementation("org.spockframework:spock-core:2.3-groovy-3.0")
}

tasks.jar {
  manifest {
    attributes["Automatic-Module-Name"] = "org.ajoberstar.stutter"
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

stutter {
  val java11 by matrices.creating {
    javaToolchain {
      languageVersion = JavaLanguageVersion.of(11)
    }
    gradleVersions {
      compatibleRange("7.0")
    }
  }

  val java17 by matrices.creating {
    javaToolchain {
      languageVersion = JavaLanguageVersion.of(17)
    }
    gradleVersions {
      compatibleRange("7.3")
    }
  }
}

tasks.check {
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
