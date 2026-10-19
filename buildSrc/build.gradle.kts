plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Provides the IntelliJ Markdown parser for build-time markdown-to-HTML conversion
    implementation("org.jetbrains:markdown-jvm:0.7.16") // https://github.com/JetBrains/markdown
}
