import groovy.json.JsonSlurper
import icu.windea.pls.gradle.markdown.ChangelogSupport
import icu.windea.pls.gradle.markdown.MarkdownSupport
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.extensions.excludeCoroutines
import org.jetbrains.intellij.platform.gradle.extensions.excludeKotlinStdlib
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

plugins {
    id("org.jetbrains.kotlin.jvm") // https://kotlinlang.org/docs/gradle.html
    id("org.jetbrains.kotlin.plugin.serialization") // https://kotlinlang.org/docs/serialization.html
    id("org.jetbrains.intellij.platform") // https://github.com/JetBrains/intellij-platform-gradle-plugin
    id("org.jetbrains.intellij.platform.grammarkit") // https://github.com/JetBrains/intellij-platform-gradle-plugin
    id("org.jetbrains.kotlinx.kover") // https://github.com/Kotlin/kotlinx-kover
    // id("org.jetbrains.changelog") // https://github.com/JetBrains/gradle-changelog-plugin

    // Used to run benchmarks (JMH-based)
    id("me.champeau.jmh") // https://github.com/melix/jmh-gradle-plugin

    // Used to download CWT config ZIPs (HTTPS) on demand when local repositories are missing, to support CI environments
    id("de.undercouch.download") // https://github.com/michel-kraemer/gradle-download-task
}

abstract class ChronicleBenchmarkTask : JavaExec() {
    @get:Option(option = "benchmarks", description = "Benchmark filters, like --tests.")
    @get:Input
    abstract val benchmarks: ListProperty<String>
}

abstract class ChronicleBenchmarkReportTask : DefaultTask() {
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:Internal
    abstract val outputBaseDir: DirectoryProperty

    @get:Input
    abstract val jmhVersion: Property<String>

    @get:Input
    abstract val gradleVersion: Property<String>

    @get:Input
    abstract val command: Property<String>

    @TaskAction
    fun generateReport() {
        val inputPath = inputDir.get().asFile.toPath()
        val jsonFiles = inputPath.toFile().listFiles { file ->
            file.isFile && file.extension == "json" && file.name != "run.json"
        }?.sortedBy { it.name }.orEmpty()
        if (jsonFiles.isEmpty()) {
            throw GradleException("No benchmark JSON results found in $inputPath. Run `./gradlew benchmark` first.")
        }

        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val outputDir = outputBaseDir.get().dir(timestamp).asFile
        outputDir.mkdirs()
        jsonFiles.forEach { it.copyTo(File(outputDir, it.name), overwrite = true) }
        val runMetadata = inputPath.resolve("run.json").toFile()
        if (runMetadata.exists()) runMetadata.copyTo(File(outputDir, runMetadata.name), overwrite = true)

        val rows = readRows(jsonFiles)
        File(outputDir, "summary.md").writeText(buildSummary(rows, timestamp))
        rows.groupBy { it.className }.forEach { (className, classRows) ->
            File(outputDir, "${className.substringAfterLast('.')}.txt").writeText(buildClassReport(className, classRows))
        }
        File(outputDir, "metadata.md").writeText(buildMetadata(timestamp))
        logger.lifecycle("Benchmark report: ${outputDir.absolutePath}")
    }

    private fun readRows(jsonFiles: List<File>): List<BenchmarkRow> {
        val slurper = JsonSlurper()
        val rows = mutableListOf<BenchmarkRow>()
        for (file in jsonFiles) {
            val entries = slurper.parse(file) as? List<*> ?: continue
            for (entry in entries) {
                val map = entry as? Map<*, *> ?: continue
                val benchmark = map["benchmark"]?.toString() ?: continue
                val primaryMetric = map["primaryMetric"] as? Map<*, *> ?: continue
                val params = (map["params"] as? Map<*, *>)
                    ?.entries
                    ?.joinToString(", ") { (key, value) -> "$key=$value" }
                    .orEmpty()
                rows.add(
                    BenchmarkRow(
                        benchmark = benchmark,
                        params = params,
                        mode = map["mode"]?.toString().orEmpty(),
                        score = (primaryMetric["score"] as? Number)?.toDouble() ?: Double.NaN,
                        error = (primaryMetric["scoreError"] as? Number)?.toDouble() ?: Double.NaN,
                        unit = primaryMetric["scoreUnit"]?.toString().orEmpty(),
                        forks = (map["forks"] as? Number)?.toInt() ?: 0,
                    )
                )
            }
        }
        return rows.sortedBy { it.benchmark }
    }

    private fun buildSummary(rows: List<BenchmarkRow>, timestamp: String): String = buildString {
        appendLine("# Benchmark Report")
        appendLine()
        appendLine("- Timestamp: $timestamp")
        appendLine("- JMH version: ${jmhVersion.get()}")
        appendLine()
        appendLine("| Benchmark | Params | Mode | Score | Error | Unit | Forks |")
        appendLine("|---|---|---|---:|---:|---|---:|")
        for (row in rows) appendRow(row)
    }

    private fun buildClassReport(className: String, rows: List<BenchmarkRow>): String = buildString {
        appendLine("# $className")
        appendLine()
        appendLine("| Benchmark | Params | Mode | Score | Error | Unit | Forks |")
        appendLine("|---|---|---|---:|---:|---|---:|")
        for (row in rows) appendRow(row)
    }

    private fun StringBuilder.appendRow(row: BenchmarkRow) {
        append("| ${row.benchmark.escapeTable()} | ${row.params.escapeTable()} | ${row.mode.escapeTable()} | ")
        append("${formatNumber(row.score)} | ${formatNumber(row.error)} | ${row.unit.escapeTable()} | ${row.forks} |")
        appendLine()
    }

    private fun buildMetadata(timestamp: String): String {
        val runInfo = inputDir.get().file("run.json").asFile
            .takeIf { it.exists() }
            ?.let { JsonSlurper().parse(it) as? Map<*, *> }
            .orEmpty()
        fun info(key: String, fallback: String): String = runInfo[key]?.toString() ?: fallback
        return buildString {
            appendLine("# Benchmark Metadata")
            appendLine()
            appendLine("- Timestamp: $timestamp")
            appendLine("- JMH version: ${info("jmhVersion", jmhVersion.get())}")
            appendLine("- Gradle version: ${gradleVersion.get()}")
            appendLine("- Java: ${info("javaVersion", System.getProperty("java.version"))} (${info("javaVendor", System.getProperty("java.vendor"))})")
            appendLine(
                "- OS: ${info("osName", System.getProperty("os.name"))} " +
                    "${info("osVersion", System.getProperty("os.version"))} " +
                    "(${info("osArch", System.getProperty("os.arch"))})",
            )
            appendLine("- Processors: ${info("processors", Runtime.getRuntime().availableProcessors().toString())}")
            appendLine("- Git commit: ${exec("git", "rev-parse", "--short", "HEAD") ?: "unknown"}")
            appendLine("- Git branch: ${exec("git", "rev-parse", "--abbrev-ref", "HEAD") ?: "unknown"}")
            val dirty = exec("git", "status", "--porcelain")
            appendLine("- Git dirty: ${if (dirty.isNullOrBlank()) "false" else "true"}")
            appendLine("- Command: ${command.get()}")
            val benchmarkNames = (runInfo["benchmarks"] as? List<*>)
                ?.mapNotNull { (it as? Map<*, *>)?.get("className")?.toString() }
                ?.joinToString(", ")
                .orEmpty()
            appendLine("- Benchmarks: $benchmarkNames")
        }
    }

    private fun exec(vararg command: String): String? = runCatching {
        val process = ProcessBuilder(*command).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText().trim()
        if (process.waitFor() == 0) output else null
    }.getOrNull()

    private fun formatNumber(value: Double): String {
        if (value.isNaN()) return "NaN"
        return String.format(Locale.ROOT, "%.3f", value)
    }

    private fun String.escapeTable(): String = replace("|", "\\|")

    private data class BenchmarkRow(
        val benchmark: String,
        val params: String,
        val mode: String,
        val score: Double,
        val error: Double,
        val unit: String,
        val forks: Int,
    ) {
        val className: String get() = benchmark.substringBeforeLast('.')
    }
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

        // 从 CHANGELOG.md 中提取当前版本的变更说明，并转换为 HTML
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

        // IDE 安装器不提供 EAP 版本，针对 EAP 需要切换到多平台归档（useInstaller = false）
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

// Configure JMH Gradle Plugin - read more: https://github.com/melix/jmh-gradle-plugin
jmh {
    jmhVersion = "1.37"
    // Benchmark sources live in `src/jmh` and must not pull in the test source set
    includeTests = false
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

// region CWT Config Source Setup

// Configurable parameters for download behavior (override via -P)
val cwtDownloadIfMissing = providers.gradleProperty("chronicle.cwt.downloadIfMissing").orElse("true")
val cwtAcceptAnyCertificate = providers.gradleProperty("chronicle.cwt.acceptAnyCertificate").orElse("false")

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
    withType<AbstractArchiveTask> {
        // Rename the plugin archive
        archiveBaseName = providers.gradleProperty("pluginPackageName")
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
    register<ChronicleBenchmarkTask>("benchmark") {
        group = "verification"
        description = "Run JMH benchmarks. Use --benchmarks to filter benchmarks, like --tests."

        dependsOn("jmhCompileGeneratedClasses")
        mainClass.set("icu.windea.pls.test.ChronicleBenchmarkLauncher")
        classpath = files(
            project.sourceSets["main"].output,
            project.sourceSets["jmh"].output,
            project.layout.buildDirectory.dir("jmh-generated-classes"),
            project.layout.buildDirectory.dir("jmh-generated-resources"),
            project.sourceSets["jmh"].runtimeClasspath,
        )

        System.getProperties().stringPropertyNames()
            .filter { it.startsWith("chronicle.") }
            .forEach { k -> systemProperty(k, System.getProperty(k)) }
        systemProperty(
            "chronicle.benchmark.classesDir",
            System.getProperty("chronicle.benchmark.classesDir")
                ?: project.sourceSets["jmh"].output.classesDirs.asPath,
        )
        systemProperty(
            "chronicle.benchmark.reportDir",
            System.getProperty("chronicle.benchmark.reportDir")
                ?: project.layout.buildDirectory.dir("reports/benchmarks").get().asFile.absolutePath,
        )

        argumentProviders.add(CommandLineArgumentProvider { benchmarks.getOrElse(emptyList()) })
        outputs.upToDateWhen { false }
    }
    register<ChronicleBenchmarkReportTask>("benchmarkReport") {
        group = "verification"
        description = "Run benchmarks and generate timestamped JSON/TEXT reports."
        dependsOn("benchmark")
        val reportDirProperty = System.getProperty("chronicle.benchmark.reportDir")
        if (reportDirProperty != null) {
            inputDir.set(project.file(reportDirProperty))
        } else {
            inputDir.set(project.layout.buildDirectory.dir("reports/benchmarks"))
        }
        outputBaseDir.set(project.layout.projectDirectory.dir("documents/reports/benchmarks"))
        jmhVersion.set("1.37")
        gradleVersion.set(project.gradle.gradleVersion)
        command.set(project.gradle.startParameter.taskRequests.joinToString(" ") { it.args.joinToString(" ") })
        outputs.upToDateWhen { false }
    }
    withType<AbstractArchiveTask> {
        // Ensure the latest built-in config files are read
        // Keep archive entry timestamps consistent with file write times (not normalized to a constant)
        // https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives
        isPreserveFileTimestamps = true
    }
}
