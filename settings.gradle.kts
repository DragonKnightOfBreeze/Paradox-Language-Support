pluginManagement {
    val kotlinVersion = providers.gradleProperty("kotlinVersion").get()
    val intellijVersion = providers.gradleProperty("intellijVersion").get()
    val koverVersion = providers.gradleProperty("koverVersion").get()

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
        id("org.jetbrains.intellij.platform") version intellijVersion
        id("org.jetbrains.intellij.platform.grammarkit") version intellijVersion
        id("org.jetbrains.kotlinx.kover") version koverVersion
        id("me.champeau.jmh") version "0.7.3"
        id("de.undercouch.download") version "5.7.0"
    }
}
