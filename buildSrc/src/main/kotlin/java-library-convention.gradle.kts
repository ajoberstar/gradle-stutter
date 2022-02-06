plugins {
  `java-library`
  `maven-publish`
  `signing`
  id("locking-convention")
  id("spotless-convention")
}

group = "org.ajoberstar"

repositories {
  mavenCentral()
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
  }
  withSourcesJar()
  withJavadocJar()
}

publishing {
  repositories {
    maven {
      name = "CentralReleases"
      url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
      credentials {
        username = System.getenv("OSSRH_USERNAME")
        password = System.getenv("OSSRH_PASSWORD")
      }
    }

    maven {
      name = "CentralSnapshots"
      url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
      credentials {
        username = System.getenv("OSSRH_USERNAME")
        password = System.getenv("OSSRH_PASSWORD")
      }
    }
  }

  publications {
    create<MavenPublication>("main") {
      from(components["java"])

      versionMapping {
        usage("java-api") {
          fromResolutionOf("runtimeClasspath")
        }
        usage("java-runtime") {
          fromResolutionResult()
        }
      }

      pom {
        name.set("Gradle Stutter")
        description.set("A Gradle plugin plugin")
        url.set("https://github.com/ajoberstar/gradle-stutter")

        developers {
          developer {
            name.set("Andrew Oberstar")
            email.set("ajoberstar@gmail.com")
          }
        }

        licenses {
          license {
            name.set("The Apache Software License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0")
          }
        }

        scm {
          url.set("https://github.com/ajoberstar/gradle-stutter")
          connection.set("scm:git:git@github.com:ajoberstar/gradle-stutter.git")
          developerConnection.set("scm:git:ssh:git@github.com:ajoberstar/gradle-stutter.git")
        }
      }
    }
  }
}

signing {
  setRequired(System.getenv("CI"))
  val signingKey: String? by project
  val signingPassphrase: String? by project
  useInMemoryPgpKeys(signingKey, signingPassphrase)
  sign(publishing.publications["main"])
}
