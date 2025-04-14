pluginManagement {
  plugins {
    id("org.ajoberstar.defaults.gradle-plugin") version "0.17.6"

    id("org.ajoberstar.reckon.settings") version "0.19.2-beta.0.5+20250414T021000Z"
    id("org.ajoberstar.stutter") version "0.7.3"

    id("com.diffplug.spotless") version "6.25.0"
  }

  repositories {
    mavenLocal()
    mavenCentral()
  }
}

plugins {
  id("org.ajoberstar.reckon.settings")
}

extensions.configure<org.ajoberstar.reckon.gradle.ReckonExtension> {
  setDefaultInferredScope("patch")
  stages("beta", "rc", "final")
  setScopeCalc(calcScopeFromProp().or(calcScopeFromCommitMessages()))
  setStageCalc(calcStageFromProp())
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

rootProject.name = "gradle-stutter"
