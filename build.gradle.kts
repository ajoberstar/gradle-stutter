plugins {
  id("org.ajoberstar.grgit")
  id("org.ajoberstar.reckon")

  id("java-library-convention")
  id("java-gradle-plugin")
  id("groovy")

  id("com.gradle.plugin-publish")
  id("org.ajoberstar.stutter")
}

reckon {
  scopeFromProp()
  stageFromProp("alpha", "beta", "rc", "final")
}

group = "org.ajoberstar"

// avoid conflict with localGroovy()
configurations.configureEach {
  exclude(group = "org.codehaus.groovy")
}

dependencies {
  implementation("com.fasterxml.jackson.core:jackson-databind:latest.release")

  testImplementation("org.junit.jupiter:junit-jupiter-api:latest.release")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:latest.release")

  compatTestImplementation(gradleTestKit())
  compatTestImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
}

tasks.named<Jar>("jar") {
  manifest {
    attributes.put("Automatic-Module-Name", "org.ajoberstar.stutter")
  }
}

tasks.withType<Test>() {
  useJUnitPlatform()
}

stutter {
  val java8 by matrices.creating {
    javaToolchain {
      languageVersion.set(JavaLanguageVersion.of(8))
    }
    gradleVersions {
      compatibleRange("5.0")
    }
  }

  val java11 by matrices.creating {
    javaToolchain {
      languageVersion.set(JavaLanguageVersion.of(11))
    }
    gradleVersions {
      compatibleRange("5.0")
    }
  }

  val java17 by matrices.creating {
    javaToolchain {
      languageVersion.set(JavaLanguageVersion.of(17))
    }
    gradleVersions {
      compatibleRange("7.3")
    }
  }
}

pluginBundle {
  website = "https://github.com/ajoberstar/gradle-stutter"
  vcsUrl = "https://github.com/ajoberstar/gradle-stutter.git"
  description = "A Gradle plugin plugin"
  plugins {
    create("stutterPlugin") {
      id = "org.ajoberstar.stutter"
      displayName = "Stutter Plugin"
      tags = listOf("plugin-plugin", "testkit", "testing")
    }
  }
  mavenCoordinates {
    groupId = project.group as String
    artifactId = project.name as String
    version = project.version.toString()
  }
}

// remove duplicate publication
gradlePlugin {
  setAutomatedPublishing(false)
}
