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
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
        id("org.jetbrains.intellij.platform") version intellijVersion
        id("org.jetbrains.intellij.platform.grammarkit") version intellijVersion
        id("org.jetbrains.kotlinx.kover") version koverVersion
    }
}
