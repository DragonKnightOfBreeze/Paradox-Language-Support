
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.20" // https://kotlinlang.org/docs/gradle.html
    id("org.jetbrains.intellij.platform") version "2.10.5" // https://github.com/JetBrains/intellij-platform-gradle-plugin
    id("org.jetbrains.grammarkit") version "2023.3.0.1"  // https://github.com/JetBrains/gradle-grammar-kit-plugin
    id("org.jetbrains.changelog") version "2.4.0" // https://github.com/JetBrains/gradle-changelog-plugin

    // Used to download CWT config ZIPs (HTTPS) on demand when local repositories are missing, to support CI environments
    id("de.undercouch.download") version "5.6.0" // https://github.com/michel-kraemer/gradle-download-task
}

fun properties(key: String) = providers.gradleProperty(key)
fun envVars(key: String) = providers.environmentVariable(key)

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Lite build: excludes optional dependency JARs; also appends the `-lite` suffix to versions in the ZIP and plugin.xml
val lite = properties("pls.is.lite").getOrElse("false").toBoolean()
val includeSqlite = properties("pls.capabilities.includeSqlite").getOrElse("true").toBoolean()

dependencies {
    // Configure Gradle IntelliJ Plugin
    // Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
    intellijPlatform {
        val type = properties("platformType")
        val version = properties("platformVersion")
        create(type, version) // https://github.com/JetBrains/intellij-platform-plugin

        testFramework(TestFrameworkType.Platform)

        bundledPlugins("com.intellij.platform.images")
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
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.3") {
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        exclude(group = "org.jspecify", module = "jspecify")
    }

    // TwelveMonkeys - https://github.com/haraldk/TwelveMonkeys
    implementation("com.twelvemonkeys.imageio:imageio-dds:3.13.0")
    implementation("com.twelvemonkeys.imageio:imageio-tga:3.13.0")

    // javassist - https://github.com/jboss-javassist/javassist
    implementation("org.javassist:javassist:3.30.2-GA")

    // AI integration

    // LangChain4J - https://github.com/langchain4j/langchain4j
    implementation("dev.langchain4j:langchain4j:1.10.0") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("dev.langchain4j:langchain4j-open-ai:1.10.0") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("dev.langchain4j:langchain4j-anthropic:1.10.0") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("dev.langchain4j:langchain4j-ollama:1.10.0") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "com.fasterxml.jackson.core")
    }

    // Persistence

    // sqlite - https://github.com/xerial/sqlite-jdbc
    runtimeOnly("org.xerial:sqlite-jdbc:3.51.1.0")
    // ktorm - https://www.ktorm.org/
    implementation("org.ktorm:ktorm-core:4.1.1") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("org.ktorm:ktorm-support-sqlite:4.1.1") {
        exclude(group = "org.jetbrains.kotlin")
    }

    compileOnly("com.google.errorprone:error_prone_annotations:2.44.0")
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("org.slf4j:slf4j-api:2.0.17")
    compileOnly("org.jetbrains:annotations:26.0.2") // https://github.com/JetBrains/java-annotations

    // Currently for reference only

    // byte-buddy - https://github.com/raphw/byte-buddy
    testImplementation("net.bytebuddy:byte-buddy:1.18.3")
    testImplementation("net.bytebuddy:byte-buddy-agent:1.18.3")

    // jte - https://github.com/casid/jte
    testImplementation("gg.jte:jte:3.2.2")
    testCompileOnly("gg.jte:jte-kotlin:3.2.2")

    // pebble - https://github.com/PebbleTemplates/pebble
    testImplementation("io.pebbletemplates:pebble:3.2.4") {
        exclude(group = "org.slf4j", module = "slf4j-api")
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
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlin.ExperimentalStdlibApi",
            )
        )
    }
}

intellijPlatform {
    pluginConfiguration {
        id = properties("pluginId")
        name = properties("pluginName")
        version = properties("pluginVersion")

        description = projectDir.resolve("DESCRIPTION.md").readText().let(::markdownToHTML)

        // local variable for configuration cache compatibility
        val changelog = project.changelog
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                @Suppress("UNCHECKED_CAST")
                fun handleChangelogItem(changelogItem: Changelog.Item) {
                    val items = changelogItem.javaClass.getDeclaredField("items").also { it.trySetAccessible() }.get(changelogItem)
                        as? MutableMap<String, Set<String>> ?: return
                    items.keys.forEach { key ->
                        val item = items[key]
                        if (item.isNullOrEmpty()) return@forEach
                        val finalItem = item.mapNotNull {
                            when {
                                it.contains("HIDDEN") -> null // hidden
                                it.startsWith("[ ]") -> null // undo
                                it.startsWith("[x]") || it.startsWith("[X]") -> it.drop(3).trim() // done
                                else -> it.trim()
                            }
                        }.toSet()
                        items[key] = finalItem
                    }
                }

                val changelogItem0 = getOrNull(pluginVersion) ?: getUnreleased()
                val changelogItem = changelogItem0.withHeader(false).withEmptySections(false)
                handleChangelogItem(changelogItem)
                renderItem(changelogItem, Changelog.OutputType.HTML)
            }
        }

        ideaVersion {
            sinceBuild = properties("sinceBuild")
            untilBuild = null
        }
    }

    // https://plugins.jetbrains.com/docs/intellij/plugin-signing.html
    signing {
        certificateChain = envVars("CERTIFICATE_CHAIN_PLS")
        privateKey = envVars("PRIVATE_KEY_PLS")
        password = envVars("PRIVATE_KEY_PASSWORD_PLS")
    }

    publishing {
        token = envVars("IDEA_TOKEN")
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    header = version
    headerParserRegex.set("""[A-Za-z0-9.-]+""".toRegex())
    groups.empty()
    keepUnreleasedSection = true
    repositoryUrl = properties("pluginRepositoryUrl")
}

grammarKit {
    jflexRelease = "1.7.0-2"
}

val excludesInJar = emptyList<String>()
val excludesInZip = buildList {
    if (lite || !includeSqlite) {
        add("lib/sqlite-jdbc-*.jar")
    }
}

// ========== CWT config source setup (prefer local, download if missing) ==========
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
val prepareCwtConfigs = tasks.register("prepareCwtConfigs")

// Register download/unzip tasks for each downloadable repository (skip if a local copy exists)
cwtRepositories.filter { it.downloadable }.forEach { r ->
    val localDir = layout.projectDirectory.dir("cwt/${r.repoDir}")
    val zipUrl = providers.provider { r.zipUrl }
    val zipFile = layout.buildDirectory.file("tmp/cwt/${r.repoDir}.zip")
    val unzipDir = generatedCwtDir.map { it.dir(r.repoDir) }

    val download = tasks.register("downloadCwtConfig_${r.gameTypeId}", de.undercouch.gradle.tasks.download.Download::class) {
        // Only download when the local repository is missing to reduce unnecessary network traffic in CI and locally
        src(zipUrl)
        dest(zipFile)
        overwrite(false)
        if (cwtAcceptAnyCertificate.get().toBoolean()) acceptAnyCertificate(true) // If SSL handshake fails, temporarily bypass via -Ppls.cwt.acceptAnyCertificate=true
        onlyIf { cwtDownloadIfMissing.get().toBoolean() && !localDir.asFile.exists() && !zipFile.get().asFile.exists() }
    }
    val unzip = tasks.register<Copy>("unzipCwtConfig_${r.gameTypeId}") {
        // Unzip the downloaded ZIP into the generated build directory as a fallback config source
        dependsOn(download)
        onlyIf { cwtDownloadIfMissing.get().toBoolean() && !localDir.asFile.exists() }
        from({ zipTree(zipFile) })
        into(unzipDir)
    }
    prepareCwtConfigs.configure { dependsOn(unzip) }
}

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
                    // - Flatten files under the config directory
                    eachFile {
                        path = path.replace("/$gameTypeId/${r.repoDir}-${r.branch}/", "/$gameTypeId/")
                        path = path.replace("/$gameTypeId/config/", "/$gameTypeId/")
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
        if (lite) pluginVersion = properties("pluginVersion").get() + "-lite"
    }
    buildPlugin {
        if (lite) archiveVersion = properties("pluginVersion").get() + "-lite"
        // Exclude specific files
        excludesInZip.forEach { exclude(it) }
        // Rename the plugin archive
        archiveBaseName = properties("pluginPackageName")
    }
    runIde {
        systemProperty("idea.is.internal", "true")
        systemProperty("ide.slow.operations.assertion", "false")
        // systemProperty("idea.log.debug.categories", "icu.windea.pls")
        // systemProperty("pls.is.debug", "true")
        // systemProperty("pls.refresh.builtIn", "true")
        jvmArgs("-Xmx4096m")
    }
    withType<Test> {
        useJUnit()
        systemProperty("ide.slow.operations.assertion", "false")
        systemProperty("idea.log.debug.categories", "icu.windea.pls")
        systemProperty("pls.is.debug", "true")
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

    // Making this work properly is a bit too much hassle
    // val testTaskProvider = named<Test>("test")
    // register<Test>("aiTest") {
    //     group = "verification"
    //     description = "Run AI-related tests"
    //     useJUnit()
    //     // Reuse the same compiled classes and classpath as the main 'test' task
    //     testClassesDirs = testTaskProvider.get().testClassesDirs
    //     classpath = testTaskProvider.get().classpath
    //     // Only run AI tests
    //     include("icu/windea/pls/ai/**")
    //     // Avoid parallel API calls
    //     maxParallelForks = 1
    //     // Run only when API key exists to prevent false failures locally/CI
    //     onlyIf {
    //         val hasKey = System.getenv("DEEPSEEK_KEY")?.isNotBlank() == true
    //         if (!hasKey) logger.lifecycle("Skipping aiTest because DEEPSEEK_KEY is not set.")
    //         hasKey
    //     }
    // }
}
