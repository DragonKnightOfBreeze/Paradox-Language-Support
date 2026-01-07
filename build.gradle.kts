
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.20" // https://kotlinlang.org/docs/gradle.html
    id("org.jetbrains.intellij.platform") version "2.10.5" // https://github.com/JetBrains/intellij-platform-gradle-plugin
    id("org.jetbrains.grammarkit") version "2023.3.0.1"  // https://github.com/JetBrains/gradle-grammar-kit-plugin
    id("org.jetbrains.changelog") version "2.4.0" // https://github.com/JetBrains/gradle-changelog-plugin

    // 用于在缺失本地仓库时按需下载 CWT 规则 zip（HTTPS），以兼容 CI 环境
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

// Lite 版本 - 不包含可选的依赖包 & zip 和 plugin.xml 中的版本号加上 `-lite` 后缀
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

        // 用作参考
        bundledPlugins("com.intellij.java")
        bundledPlugins("org.jetbrains.kotlin")
        // bundledPlugins("JavaScript")
        // bundledPlugins("tslint")

        // TranslationPlugin - https://github.com/YiiGuxing/TranslationPlugin
        plugin("cn.yiiguxing.plugin.translate:3.8.0")
    }

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

    // AI 集成

    // LangChain4J - https://github.com/langchain4j/langchain4j
    implementation("dev.langchain4j:langchain4j:1.8.0") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("dev.langchain4j:langchain4j-open-ai:1.8.0") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("dev.langchain4j:langchain4j-anthropic:1.8.0") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("dev.langchain4j:langchain4j-ollama:1.8.0") {
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "com.fasterxml.jackson.core")
    }

    // 持久化

    // sqlite - https://github.com/xerial/sqlite-jdbc
    runtimeOnly("org.xerial:sqlite-jdbc:3.51.0.0")
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

    // 目前仅用作参考

    // byte-buddy - https://github.com/raphw/byte-buddy
    testImplementation("net.bytebuddy:byte-buddy:1.18.1")
    testImplementation("net.bytebuddy:byte-buddy-agent:1.18.1")

    // jte - https://github.com/casid/jte
    testImplementation("gg.jte:jte:3.2.1")
    testCompileOnly("gg.jte:jte-kotlin:3.2.1")

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

// ========== CWT 规则来源配置（本地优先，缺失即下载）==========
// 下载来源行为的可配置参数（通过 -P 覆盖）
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
    CwtRepository("core", "core", downloadable = false), // core 在当前仓库内，无需下载
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

// 生成产物目录（用于存放解压后的规则），本地缺失时作为备用来源
val generatedCwtDir = layout.buildDirectory.dir("generated/cwt")
// 准备任务：收拢后续下载与解压依赖，便于 jar 统一依赖
val prepareCwtConfigs = tasks.register("prepareCwtConfigs")

// 为每个可下载的仓库注册下载与解压任务（本地存在时跳过）
cwtRepositories.filter { it.downloadable }.forEach { r ->
    val localDir = layout.projectDirectory.dir("cwt/${r.repoDir}")
    val zipUrl = providers.provider { r.zipUrl }
    val zipFile = layout.buildDirectory.file("tmp/cwt/${r.repoDir}.zip")
    val unzipDir = generatedCwtDir.map { it.dir(r.repoDir) }

    val download = tasks.register("downloadCwtConfig_${r.gameTypeId}", de.undercouch.gradle.tasks.download.Download::class) {
        // 仅当本地不存在对应仓库时才尝试下载，减少 CI 与本地不必要的网络开销
        src(zipUrl)
        dest(zipFile)
        overwrite(false)
        if (cwtAcceptAnyCertificate.get().toBoolean()) acceptAnyCertificate(true) // 如遇 SSL 握手失败，可通过 -Ppls.cwt.acceptAnyCertificate=true 临时绕过
        onlyIf { cwtDownloadIfMissing.get().toBoolean() && !localDir.asFile.exists() && !zipFile.get().asFile.exists() }
    }
    val unzip = tasks.register<Copy>("unzipCwtConfig_${r.gameTypeId}") {
        // 解压下载的 zip 到构建生成目录，作为本地缺失时的备用规则来源
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
        // 统一依赖规则准备任务：确保在打包前完成本地检查与必要的下载/解压
        dependsOn(prepareCwtConfigs)
        // 添加项目文档和许可证
        from("README.md", "README_en.md", "LICENSE")
        // 排除特定文件
        excludesInJar.forEach { exclude(it) }

        // 添加规则文件（本地优先，缺失则使用解压后的备用来源）
        cwtRepositories.forEach { r ->
            val gameTypeId = r.gameTypeId
            into("config/$gameTypeId") {
                // 根据是否存在本地目录选择有效来源，避免在 CI 上缺失规则
                val effectiveSource = providers.provider {
                    val local = file("cwt/${r.repoDir}")
                    if (local.exists()) local else generatedCwtDir.get().dir(r.repoDir).asFile
                }
                from(effectiveSource) {
                    includeEmptyDirs = false
                    // 仅包含需要的文件（不包括规则目录中的 script-docs 子目录中的日志文件）
                    include("**/*.cwt", "**/LICENSE", "**/*.md")
                    // 规范路径：
                    // - 打平规则目录中的 {repoDir}-{branch} 子目录中的文件
                    // - 打平规则目录中的 config 子目录中的文件
                    eachFile {
                        path = path.replace("/$gameTypeId/${r.repoDir}-${r.branch}/", "/$gameTypeId/")
                        path = path.replace("/$gameTypeId/config/", "/$gameTypeId/")
                    }
                }
            }
        }
        // 添加相关的文档和许可证
        into("config") {
            from("cwt/README.md", "cwt/LICENSE")
        }
    }
    instrumentedJar {
        // 统一依赖规则准备任务，确保与 jar 的资源一致（覆盖运行/调试场景）
        dependsOn(prepareCwtConfigs)
        // 排除特定文件
        excludesInJar.forEach { exclude(it) }
    }
    patchPluginXml {
        if (lite) pluginVersion = properties("pluginVersion").get() + "-lite"
    }
    buildPlugin {
        if (lite) archiveVersion = properties("pluginVersion").get() + "-lite"
        // 排除特定文件
        excludesInZip.forEach { exclude(it) }
        // 重命名插件包
        archiveBaseName = properties("pluginPackageName")
    }
    runIde {
        systemProperty("idea.is.internal", "true")
        systemProperty("ide.slow.operations.assertion", "false")
        systemProperty("idea.log.debug.categories", "icu.windea.pls")
        systemProperty("pls.is.debug", "true")
    }
    withType<Test> {
        useJUnit()
        systemProperty("ide.slow.operations.assertion", "false")
        systemProperty("idea.log.debug.categories", "icu.windea.pls")
        systemProperty("pls.is.debug", "true")
        // 转发所有以 pls.test. 开头的命令行 -D 属性
        System.getProperties().stringPropertyNames()
            .filter { it.startsWith("pls.test.") }
            .forEach { k -> systemProperty(k, System.getProperty(k)) }
    }

    withType<AbstractArchiveTask> {
        // 保证读取到最新的内置规则文件
        // 让 entry 时间戳与写入时间一致（不再被规范化为常量）
        // https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives
        isPreserveFileTimestamps = true
    }

    // 让它正常工作有点太麻烦了
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
