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
  compatTestImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
}

tasks.named<Jar>("jar") {
  manifest {
    attributes.put("Automatic-Module-Name", "org.ajoberstar.stutter")
  }
}

tasks.named<Test>("test") {
  useJUnitPlatform()
}

stutter {
  setSparse(true)
  java(8) {
    compatibleRange("5.0")
  }
  java(15) {
    compatibleRange("6.3")
  }
  java(17) {
    compatibleRange("7.3")
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
