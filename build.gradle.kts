import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // Provides the IntelliJ Markdown parser for build-time markdown-to-HTML conversion
        classpath("org.jetbrains:markdown-jvm:0.7.3")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.20" // https://kotlinlang.org/docs/gradle.html
    id("org.jetbrains.intellij.platform") version "2.17.0" // https://github.com/JetBrains/intellij-platform-gradle-plugin
    id("org.jetbrains.grammarkit") version "2023.3.0.3"  // https://github.com/JetBrains/gradle-grammar-kit-plugin
    id("org.jetbrains.kotlinx.kover") version "0.9.8"  // https://github.com/Kotlin/kotlinx-kover
    // id("org.jetbrains.changelog") version "2.5.0" // https://github.com/JetBrains/gradle-changelog-plugin

    // Used to download CWT config ZIPs (HTTPS) on demand when local repositories are missing, to support CI environments
    id("de.undercouch.download") version "5.7.0" // https://github.com/michel-kraemer/gradle-download-task
}

val liteVersion = providers.gradleProperty("pls.is.lite").getOrElse("false").toBoolean()
val includeSqlite = providers.gradleProperty("pls.include.sqlite").getOrElse("true").toBoolean()

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

        description = projectDir.resolve("DESCRIPTION.md").readText().let(::markdownToHTML)

        // Extract change notes for the current version from CHANGELOG.md
        // Finds the matching h2 section by version, processes items (strips [x]/[X], removes [ ] and HIDDEN),
        // then converts the result to HTML
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            val changelogText = projectDir.resolve("CHANGELOG.md").readText()
            val sectionText = extractChangelogForVersion(changelogText, pluginVersion)
            if (sectionText.isBlank()) "" else markdownToHTML(sectionText)
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
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
        pluginVerifier()

        val type = providers.gradleProperty("platformType")
        val version = providers.gradleProperty("platformVersion")
        create(type, version) // https://github.com/JetBrains/intellij-platform-plugin

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

        // plugin("Docker:253.29346.125")
        // bundledPlugins("com.intellij.microservices.jvm")
        // plugin("intellij.ktor:253.28294.251")
    }

    // kotlin test junit - https://kotlinlang.org
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    // junit - https://github.com/junit-team/junit4
    testImplementation("junit:junit:4.13.2")
    // opentest4j - https://github.com/ota4j-team/opentest4j
    testImplementation("org.opentest4j:opentest4j:1.3.0")

    // Caffeine - https://github.com/ben-manes/caffeine
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.4") {
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        exclude(group = "org.jspecify", module = "jspecify")
    }

    // TwelveMonkeys - https://github.com/haraldk/TwelveMonkeys
    implementation("com.twelvemonkeys.imageio:imageio-dds:3.13.1")
    implementation("com.twelvemonkeys.imageio:imageio-tga:3.13.1")

    // javassist - https://github.com/jboss-javassist/javassist
    implementation("org.javassist:javassist:3.31.0-GA")

    // AI integration

    // LangChain4J - https://github.com/langchain4j/langchain4j
    implementation("dev.langchain4j:langchain4j:1.14.1") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("dev.langchain4j:langchain4j-open-ai:1.14.1") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("dev.langchain4j:langchain4j-anthropic:1.14.1") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("dev.langchain4j:langchain4j-ollama:1.14.1") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "com.fasterxml.jackson.core")
    }

    // Persistence

    // sqlite - https://github.com/xerial/sqlite-jdbc
    runtimeOnly("org.xerial:sqlite-jdbc:3.53.1.0")
    // ktorm - https://www.ktorm.org/
    implementation("org.ktorm:ktorm-core:4.1.1") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("org.ktorm:ktorm-support-sqlite:4.1.1") {
        exclude(group = "org.jetbrains.kotlin")
    }

    // Compile only

    compileOnly("com.google.errorprone:error_prone_annotations:2.49.0")
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("org.slf4j:slf4j-api:2.0.17")
    compileOnly("org.jetbrains:annotations:26.1.0") // https://github.com/JetBrains/java-annotations

    // Currently for reference only

    // byte-buddy - https://github.com/raphw/byte-buddy
    testImplementation("net.bytebuddy:byte-buddy:1.18.8")
    testImplementation("net.bytebuddy:byte-buddy-agent:1.18.8")

    // jte - https://github.com/casid/jte
    testImplementation("gg.jte:jte:3.2.4")
    testCompileOnly("gg.jte:jte-kotlin:3.2.4")

    // pebble - https://github.com/PebbleTemplates/pebble
    testImplementation("io.pebbletemplates:pebble:4.1.1") {
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
        java.srcDirs("src/test/unused") // unused codes (NOTE: cannot move to another source set)
        resources.srcDirs("src/test/resources")
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        freeCompilerArgs.addAll(
            listOf(
                "-Xjvm-default=all",
                "-Xinline-classes",
                "-Xcontext-parameters",
                "-Xannotation-default-target=param-property",
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlin.ExperimentalStdlibApi",
                // "-XXLanguage:+WhenGuards",
            )
        )
    }
}

// region Methods for Markdown

// Custom markdown-to-HTML conversion using IntelliJ Markdown parser
private fun markdownToHTML(markdown: String): String {
    // Normalize text to LF, because a Markdown library currently fully supports only this line separator
    val text = markdown.normalizeLineSeparator()
    val flavour = GFMFlavourDescriptor()
    val ast = MarkdownParser(flavour).buildMarkdownTreeFromString(text)
    return HtmlGenerator(text, ast, flavour).generateHtml().normalizeLineSeparator()
}

// Normalize text to LF
private fun String.normalizeLineSeparator() = replace("\\R".toRegex(), "\n")

// Extract and process the changelog section for a given version from the changelog document
private fun extractChangelogForVersion(fullChangelog: String, targetVersion: String): String {
    // Match h2 headings: `## <heading>`
    val headingRegex = """^##\s+(.+)$""".toRegex()
    // Match version from heading text: `3.0.0`, `3.0.0-dev`, `3.0.0-dev-262`, `3.0.0 - 2026-01-01`, etc.
    val versionRegex = """^(\d+(?:\.\d+)+(?:-[A-Za-z0-9.-]+)?)(?:\s+-\s+.*)?$""".toRegex()

    // Parse the changelog into heading-content sections
    data class Section(val heading: String, val content: String)

    val sections = mutableListOf<Section>()
    var currentHeading = ""
    var currentContent = StringBuilder()

    for (line in fullChangelog.lines()) {
        val match = headingRegex.matchEntire(line)
        if (match != null) {
            if (currentHeading.isNotEmpty()) {
                sections.add(Section(currentHeading, currentContent.toString().trim()))
            }
            currentHeading = match.groupValues[1].trim()
            currentContent = StringBuilder()
        } else {
            if (currentContent.isNotEmpty()) currentContent.appendLine()
            currentContent.append(line)
        }
    }
    if (currentHeading.isNotEmpty()) {
        sections.add(Section(currentHeading, currentContent.toString().trim()))
    }

    // Find the matching section for the target version
    var targetContent: String? = null

    // 1. Try exact version match against the heading text
    for (section in sections) {
        val vMatch = versionRegex.matchEntire(section.heading)
        if (vMatch != null && vMatch.groupValues[1] == targetVersion) {
            targetContent = section.content
            break
        }
    }

    // 2. Fallback: use the "Unreleased" section if non-empty
    if (targetContent == null) {
        val unreleased = sections.find { it.heading == "Unreleased" }?.content
        if (!unreleased.isNullOrBlank()) {
            targetContent = unreleased
        }
    }

    // 3. Last fallback: use the first version-matching section
    if (targetContent == null) {
        targetContent = sections.firstOrNull { versionRegex.matches(it.heading) }?.content ?: ""
    }

    return processChangelogSection(targetContent)
}

// Process changelog section content: filter out undone/hidden items, strip checkbox markers
private fun processChangelogSection(content: String): String {
    val lines = content.lines()
    val processed = lines.mapNotNull { line ->
        val line1 = line.trimStart().lowercase()
        when {
            line1.contains("HIDDEN") -> null // Exclude hidden items
            line1.startsWith("- [ ] ") -> null // Exclude not-done items
            line1.startsWith("- [x] ") -> line.replaceFirst("- [x] ", "- ") // Convert to normal list item
            else -> line // Keep as-is
        }
    }
    return processed.joinToString("\n").trim()
}

// endregion

// region CWT Config Source Setup

// Configurable parameters for download behavior (override via -P)
val cwtDownloadIfMissing = providers.gradleProperty("pls.cwt.downloadIfMissing").orElse("true")
val cwtAcceptAnyCertificate = providers.gradleProperty("pls.cwt.acceptAnyCertificate").orElse("false")

data class CwtRepository(
    val repoDir: String,
    val gameTypeId: String,
    val owner: String = "DragonKnightOfBreeze",
    val branch: String = "master",
    val downloadable: Boolean = true,
) {
    val zipUrl = "https://github.com/${owner}/${repoDir}/archive/refs/heads/${branch}.zip"
}

val cwtRepositories = listOf(
    CwtRepository("core", "core", downloadable = false), // core is in this repository; no download needed
    CwtRepository("cwtools-ck2-config", "ck2"),
    CwtRepository("cwtools-ck3-config", "ck3"),
    CwtRepository("cwtools-eu4-config", "eu4"),
    CwtRepository("cwtools-eu5-config", "eu5"),
    CwtRepository("cwtools-hoi4-config", "hoi4"),
    CwtRepository("cwtools-ir-config", "ir"),
    CwtRepository("cwtools-stellaris-config", "stellaris"),
    CwtRepository("cwtools-vic2-config", "vic2"),
    CwtRepository("cwtools-vic3-config", "vic3"),
)

// Generated output directory (stores unzipped configs); used as a fallback when local configs are missing
val generatedCwtDir = layout.buildDirectory.dir("generated/cwt")
// Preparation task: collects downstream download/unzip dependencies so the JAR task can depend on a single task
val prepareCwtConfigs = tasks.register("prepareCwtConfigs") {
    description = "Prepare CWT configs."
}

// Register download/unzip tasks for each downloadable repository (skip if a local copy exists)
cwtRepositories.filter { it.downloadable }.forEach { r ->
    val localDir = layout.projectDirectory.dir("cwt/${r.repoDir}")
    val zipUrl = providers.provider { r.zipUrl }
    val zipFile = layout.buildDirectory.file("tmp/cwt/${r.repoDir}.zip")
    val unzipDir = generatedCwtDir.map { it.dir(r.repoDir) }

    val download = tasks.register("downloadCwtConfig_${r.gameTypeId}", de.undercouch.gradle.tasks.download.Download::class) {
        description = "Download CWT configs for game type id ${r.gameTypeId}."
        // Only download when the local repository is missing to reduce unnecessary network traffic in CI and locally
        src(zipUrl)
        dest(zipFile)
        overwrite(false)
        if (cwtAcceptAnyCertificate.get().toBoolean()) acceptAnyCertificate(true) // If SSL handshake fails, temporarily bypass via -Ppls.cwt.acceptAnyCertificate=true
        onlyIf { cwtDownloadIfMissing.get().toBoolean() && !localDir.asFile.exists() && !zipFile.get().asFile.exists() }
    }
    val unzip = tasks.register<Copy>("unzipCwtConfig_${r.gameTypeId}") {
        description = "Unzip CWT configs for game type id ${r.gameTypeId}."
        // Unzip the downloaded ZIP into the generated build directory as a fallback config source
        dependsOn(download)
        onlyIf { cwtDownloadIfMissing.get().toBoolean() && !localDir.asFile.exists() }
        from({ zipTree(zipFile) })
        into(unzipDir)
    }
    prepareCwtConfigs.configure { dependsOn(unzip) }
}

// endregion

tasks {
    withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    jar {
        // Depend on the config preparation task: ensure local checks and any required download/unzip complete before packaging
        dependsOn(prepareCwtConfigs)
        // Include project docs and license
        from("README_zh.md", "README.md", "LICENSE")
        // Exclude specific files
        excludesInJar.forEach { exclude(it) }

        // Include config files (prefer local; otherwise use the unzipped fallback source)
        cwtRepositories.forEach { r ->
            val gameTypeId = r.gameTypeId
            into("config/$gameTypeId") {
                // Choose an effective source based on whether the local directory exists, to avoid missing configs in CI
                val effectiveSource = providers.provider {
                    val local = file("cwt/${r.repoDir}")
                    if (local.exists()) local else generatedCwtDir.get().dir(r.repoDir).asFile
                }
                from(effectiveSource) {
                    includeEmptyDirs = false
                    // Include only required files (exclude log files under the script-docs subdirectory)
                    include("**/*.cwt", "**/LICENSE", "**/*.md")
                    // Normalize paths:
                    // - Flatten files under the {repoDir}-{branch} directory
                    // - Flatten files under the `config` directory
                    eachFile {
                        path = path.replace("/$gameTypeId/${r.repoDir}-${r.branch}/", "/$gameTypeId/", ignoreCase = true)
                        path = path.replace("/$gameTypeId/config/", "/$gameTypeId/", ignoreCase = true)
                    }
                }
            }
        }
        // Include related docs and license
        into("config") {
            from("cwt/README_zh.md", "cwt/LICENSE")
        }
    }
    instrumentedJar {
        // Depend on the config preparation task to keep resources in sync with the regular JAR (covers run/debug scenarios)
        dependsOn(prepareCwtConfigs)
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
        // Rename the plugin archive
        archiveBaseName = providers.gradleProperty("pluginPackageName")
    }
    runIde {
        jvmArgs("-Xmx4096m")

        systemProperty("idea.is.internal", "true")
        systemProperty("ide.slow.operations.assertion", "false")
        // systemProperty("idea.log.debug.categories", "icu.windea.pls")

        // systemProperty("chronicle.capacities.recordCacheStats", "true")
        // systemProperty("chronicle.capacities.recordIndexStats", "true")
        // systemProperty("chronicle.capacities.refreshBuiltInConfigDirectories", "true")
        // systemProperty("chronicle.capacities.keepOptionConfigs", "true")
    }
    withType<Test> {
        useJUnit()

        systemProperty("ide.slow.operations.assertion", "false")
        systemProperty("idea.log.debug.categories", "icu.windea.pls")
        // systemProperty("idea.log.debug.categories", "icu.windea.pls")

        // Forward all command-line -D properties that start with "pls.test."
        System.getProperties().stringPropertyNames()
            .filter { it.startsWith("pls.test.") }
            .forEach { k -> systemProperty(k, System.getProperty(k)) }
    }
    withType<AbstractArchiveTask> {
        // Ensure the latest built-in config files are read
        // Keep archive entry timestamps consistent with file write times (not normalized to a constant)
        // https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives
        isPreserveFileTimestamps = true
    }
}
