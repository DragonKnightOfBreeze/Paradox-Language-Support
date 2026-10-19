import icu.windea.pls.gradle.markdown.ChangelogSupport
import icu.windea.pls.gradle.markdown.MarkdownSupport
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.extensions.excludeCoroutines
import org.jetbrains.intellij.platform.gradle.extensions.excludeKotlinStdlib
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm") // https://kotlinlang.org/docs/gradle.html
    id("org.jetbrains.kotlin.plugin.serialization") // https://kotlinlang.org/docs/serialization.html
    id("org.jetbrains.intellij.platform") // https://github.com/JetBrains/intellij-platform-gradle-plugin
    id("org.jetbrains.intellij.platform.grammarkit") // https://github.com/JetBrains/intellij-platform-gradle-plugin
    id("org.jetbrains.kotlinx.kover") // https://github.com/Kotlin/kotlinx-kover
    // id("org.jetbrains.changelog") // https://github.com/JetBrains/gradle-changelog-plugin

    // Benchmark infrastructure (JMH-based)
    id("chronicle.benchmark")

    // CWT config source setup (download/unzip and include in JAR)
    id("chronicle.cwt-config")
}

val liteVersion = providers.gradleProperty("chronicle.is.lite").getOrElse("false").toBoolean()
val includeSqlite = providers.gradleProperty("chronicle.include.sqlite").getOrElse("true").toBoolean()

val excludesInJar = emptyList<String>()
val excludesInZip = buildList {
    if (liteVersion || !includeSqlite) add("lib/sqlite-jdbc-*.jar")
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

// Configure IntelliJ Platform Gradle Plugin - read more: https://github.com/JetBrains/intellij-platform-gradle-plugin
intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        description = MarkdownSupport.toHtml(projectDir.resolve("DESCRIPTION.md").readText())

        // Extract change notes for the current version from CHANGELOG.md, then converts the result to HTML
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            val changelogText = projectDir.resolve("CHANGELOG.md").readText()
            val sectionText = ChangelogSupport.extractSectionForVersion(changelogText, pluginVersion)
            if (sectionText.isBlank()) "" else MarkdownSupport.toHtml(sectionText)
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            // untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }

    // https://plugins.jetbrains.com/docs/intellij/plugin-signing.html
    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN_CHRONICLE")
        privateKey = providers.environmentVariable("PRIVATE_KEY_CHRONICLE")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD_CHRONICLE")
    }

    publishing {
        token = providers.environmentVariable("IDEA_TOKEN")
    }

    pluginVerification {
        failureLevel = listOf(
            VerifyPluginTask.FailureLevel.COMPATIBILITY_PROBLEMS, // not expected
            // VerifyPluginTask.FailureLevel.INTERNAL_API_USAGES, // have to use atm (few situations, but should be ignored by the verifier)
            VerifyPluginTask.FailureLevel.SCHEDULED_FOR_REMOVAL_API_USAGES, // not have to use atm
            VerifyPluginTask.FailureLevel.OVERRIDE_ONLY_API_USAGES, // not have to use atm
            VerifyPluginTask.FailureLevel.NON_EXTENDABLE_API_USAGES, // not have to use atm
        )

        ides {
            recommended()
        }
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        val platformType = providers.gradleProperty("platformType")
        val platformVersion = providers.gradleProperty("platformVersion")
        val platformUseInstaller = providers.gradleProperty("platformUseInstaller").getOrElse("true").toBoolean()

        // The IDE installer does not provide EAP versions, so for EAP, we need to switch to the multi-platform archive (useInstaller = false).
        // https://github.com/JetBrains/intellij-platform-plugin
        create(platformType, platformVersion) { useInstaller = platformUseInstaller }

        pluginVerifier()

        testFramework(TestFrameworkType.Platform)

        bundledPlugins("com.intellij.platform.images")
        bundledPlugins("com.intellij.modules.json")
        bundledPlugins("org.intellij.plugins.markdown")
        bundledPlugins("com.intellij.diagram")

        // For reference only
        bundledPlugins("com.intellij.java")
        bundledPlugins("org.jetbrains.kotlin")
        // bundledPlugins("JavaScript")
        // bundledPlugins("tslint")

        // TranslationPlugin - https://github.com/YiiGuxing/TranslationPlugin
        plugin("cn.yiiguxing.plugin.translate:3.8.0")
    }

    // kotlin test junit - https://kotlinlang.org
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit") {
        // The exclusion here is necessary, see: https://github.com/JetBrains/intellij-platform-gradle-plugin/issues/2188
        excludeKotlinStdlib()
        excludeCoroutines()
    }
    // junit - https://github.com/junit-team/junit4
    testImplementation("junit:junit:4.13.2")
    // opentest4j - https://github.com/ota4j-team/opentest4j
    testImplementation("org.opentest4j:opentest4j:1.3.0")

    // Caffeine - https://github.com/ben-manes/caffeine
    implementation("com.github.ben-manes.caffeine:caffeine:3.3.0") {
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        exclude(group = "org.jspecify", module = "jspecify")
    }

    // Guava and fastutil are provided by the IntelliJ Platform at runtime; benchmarks run outside the IDE need them explicitly
    jmh("com.google.guava:guava:33.2.1-jre")
    jmh("it.unimi.dsi:fastutil:8.5.19")

    // TwelveMonkeys - https://github.com/haraldk/TwelveMonkeys
    implementation("com.twelvemonkeys.imageio:imageio-dds:3.15.2")
    implementation("com.twelvemonkeys.imageio:imageio-tga:3.15.2")

    // javassist - https://github.com/jboss-javassist/javassist
    implementation("org.javassist:javassist:3.33.0-GA")

    // kotlinx-serialization - https://github.com/Kotlin/kotlinx.serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    // AI integration

    // LangChain4J - https://github.com/langchain4j/langchain4j
    implementation("dev.langchain4j:langchain4j:1.21.0") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("dev.langchain4j:langchain4j-open-ai:1.21.0") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("dev.langchain4j:langchain4j-anthropic:1.21.0") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("dev.langchain4j:langchain4j-ollama:1.21.0") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }

    // Persistence

    // sqlite - https://github.com/xerial/sqlite-jdbc
    runtimeOnly("org.xerial:sqlite-jdbc:3.53.4.0")
    // ktorm - https://www.ktorm.org/
    implementation("org.ktorm:ktorm-core:4.2.1") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("org.ktorm:ktorm-support-sqlite:4.2.1") {
        exclude(group = "org.jetbrains.kotlin")
    }

    // Compile only

    compileOnly("com.google.errorprone:error_prone_annotations:2.50.0")
    compileOnly("org.jspecify:jspecify:1.0.1")
    compileOnly("org.slf4j:slf4j-api:2.0.20")
    compileOnly("org.jetbrains:annotations:26.1.0") // https://github.com/JetBrains/java-annotations

    // Currently for reference only

    // byte-buddy - https://github.com/raphw/byte-buddy
    testImplementation("net.bytebuddy:byte-buddy:1.18.14")
    testImplementation("net.bytebuddy:byte-buddy-agent:1.18.14")

    // jte - https://github.com/casid/jte
    testImplementation("gg.jte:jte:3.2.4")
    testCompileOnly("gg.jte:jte-kotlin:3.2.4")

    // pebble - https://github.com/PebbleTemplates/pebble
    testImplementation("io.pebbletemplates:pebble:4.1.2") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
}

// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
kover {
    reports {
        total {
            xml {
                onCheck = true
            }
        }
    }
}

sourceSets {
    main {
        java.srcDirs("src/main/java", "src/main/kotlin", "src/main/gen")
        resources.srcDirs("src/main/resources")
    }
    test {
        java.srcDirs("src/test/java", "src/test/kotlin")
        java.srcDirs("src/test/unused") // unused codes (cannot move to another source set)
        resources.srcDirs("src/test/resources")
    }
}

kotlin {
    val jdkVersion = providers.gradleProperty("jdkVersion")

    jvmToolchain(jdkVersion.get().toInt())
    // https://kotlinlang.org/docs/gradle-compiler-options.html#all-compiler-options
    // https://kotlinlang.org/docs/compiler-reference.html
    compilerOptions {
        javaParameters = true
        jvmTarget = JvmTarget.fromTarget(jdkVersion.get())
        optIn.addAll(
            "kotlin.RequiresOptIn",
            "kotlin.ExperimentalStdlibApi",
            "kotlinx.serialization.ExperimentalSerializationApi",
        )
        freeCompilerArgs.addAll(
            "-java-parameters",
            "-Xno-call-assertions", // disable not-null check to optimize performance
            "-Xno-receiver-assertions", // disable not-null check to optimize performance
            "-Xno-param-assertions", // disable not-null check to optimize performance
            "-Xjvm-default=all",
            "-Xinline-classes",
            "-Xcontext-parameters",
            "-Xannotation-default-target=param-property", // see: https://kotlinlang.org/docs/whatsnew22.html#new-defaulting-rules-for-use-site-annotation-targets
            "-XXLanguage:+MultiDollarInterpolation",
            // "-Xwhen-expressions=indy", // see: https://kotlinlang.org/docs/whatsnew2220.html#kotlin-jvm-support-invokedynamic-with-when-expressions
            // -Xname-based-destructuring=only-syntax //see: https://kotlinlang.org/docs/whatsnew2320.html#name-based-destructuring
            // "-Xcollection-literals", // https://kotlinlang.org/docs/whatsnew24.html#support-for-collection-literals
        )
    }
}

tasks {
    withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    withType<AbstractArchiveTask> {
        // Rename the plugin archive
        archiveBaseName = providers.gradleProperty("pluginPackageName")
    }
    jar {
        // Include project docs and license
        from("README_zh.md", "README.md", "LICENSE")
        // Exclude specific files
        excludesInJar.forEach { exclude(it) }
    }
    instrumentedJar {
        // Exclude specific files
        excludesInJar.forEach { exclude(it) }
    }
    patchPluginXml {
        if (liteVersion) pluginVersion = providers.gradleProperty("pluginVersion").get() + "-lite"
    }
    buildPlugin {
        if (liteVersion) archiveVersion = providers.gradleProperty("pluginVersion").get() + "-lite"
        // Exclude specific files
        excludesInZip.forEach { exclude(it) }
    }
    runIde {
        jvmArgs("-Xmx4096m")

        systemProperty("idea.is.internal", "true")
        systemProperty("ide.slow.operations.assertion", "false")
        // systemProperty("idea.log.debug.categories", "icu.windea.pls")
    }
    withType<Test> {
        useJUnit()

        systemProperty("idea.is.internal", "true")
        systemProperty("ide.slow.operations.assertion", "false")
        // systemProperty("idea.log.debug.categories", "icu.windea.pls")

        // Forward all command-line -D properties that start with "chronicle.test."
        System.getProperties().stringPropertyNames()
            .filter { it.startsWith("chronicle.test.") }
            .forEach { k -> systemProperty(k, System.getProperty(k)) }
    }
    withType<AbstractArchiveTask> {
        // Ensure the latest built-in config files are read
        // Keep archive entry timestamps consistent with file write times (not normalized to a constant)
        // https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives
        isPreserveFileTimestamps = true
    }
}
