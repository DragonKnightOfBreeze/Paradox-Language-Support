plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Provides the IntelliJ Markdown parser for build-time markdown-to-HTML conversion
    implementation("org.jetbrains:markdown-jvm:0.7.16") // https://github.com/JetBrains/markdown
    // Provides the download task used by the CWT config source setup
    implementation("de.undercouch:gradle-download-task:5.7.0") // https://github.com/michel-kraemer/gradle-download-task
    // Provides the JMH Gradle plugin and its extension types
    implementation("me.champeau.jmh:me.champeau.jmh.gradle.plugin:0.7.3") // https://github.com/melix/jmh-gradle-plugin
    // Used to parse JMH JSON results
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1") // https://github.com/Kotlin/kotlinx.serialization
}

gradlePlugin {
    plugins {
        register("benchmark") {
            id = "chronicle.benchmark"
            implementationClass = "icu.windea.pls.gradle.benchmark.ChronicleBenchmarkPlugin"
        }
        register("cwtConfig") {
            id = "chronicle.cwt-config"
            implementationClass = "icu.windea.pls.gradle.cwt.CwtConfigPlugin"
        }
    }
}
