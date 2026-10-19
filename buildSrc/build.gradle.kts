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
}

gradlePlugin {
    plugins {
        register("cwtConfig") {
            id = "chronicle.cwt-config"
            implementationClass = "icu.windea.pls.gradle.cwt.CwtConfigPlugin"
        }
    }
}
