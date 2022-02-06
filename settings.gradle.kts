pluginManagement {
  plugins {
    id("com.gradle.plugin-publish") version "0.20.0"

    id("org.ajoberstar.grgit") version "4.1.1"
    id("org.ajoberstar.reckon") version "0.13.1"
    id("org.ajoberstar.stutter") version "0.6.0"

    id("com.diffplug.spotless") version "6.2.1"
  }
}

rootProject.name = "gradle-stutter"
