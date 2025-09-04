import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.10" // https://kotlinlang.org/docs/gradle.html
    id("org.jetbrains.intellij.platform") version "2.8.0" // https://github.com/JetBrains/intellij-platform-gradle-plugin
    id("org.jetbrains.grammarkit") version "2022.3.2.2"  // https://github.com/JetBrains/gradle-grammar-kit-plugin
    id("org.jetbrains.changelog") version "2.4.0" // https://github.com/JetBrains/gradle-changelog-plugin
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
        plugin("cn.yiiguxing.plugin.translate:3.7.3")
    }

    // junit - https://github.com/junit-team/junit4
    testImplementation("junit:junit:4.13.2")
    // opentest4j - https://github.com/ota4j-team/opentest4j
    testImplementation("org.opentest4j:opentest4j:1.3.0")

    // Caffeine - https://github.com/ben-manes/caffeine
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.2")

    // TwelveMonkeys - https://github.com/haraldk/TwelveMonkeys
    implementation("com.twelvemonkeys.imageio:imageio-dds:3.12.0")
    implementation("com.twelvemonkeys.imageio:imageio-tga:3.12.0")

    // dds - https://github.com/iTitus/dds
    implementation("io.github.ititus:dds:3.1.0")
    // implementation("io.github.ititus:ddsiio:3.1.0")

    // javassist - https://github.com/jboss-javassist/javassist
    implementation("org.javassist:javassist:3.30.2-GA")

    // pebble - https://github.com/PebbleTemplates/pebble
    implementation("io.pebbletemplates:pebble:3.2.4")

    // AI 集成

    // LangChain4J - https://github.com/langchain4j/langchain4j
    implementation("dev.langchain4j:langchain4j:1.4.0") {
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("dev.langchain4j:langchain4j-open-ai:1.4.0") {
        exclude(group = "com.fasterxml.jackson.core")
    }
    // implementation("dev.langchain4j:langchain4j-kotlin:1.4.0-beta10") {
    //     exclude(group = "org.jetbrains.kotlin")
    //     exclude(group = "org.jetbrains.kotlinx")
    //     exclude(group = "com.fasterxml.jackson.core")
    // }

    // 持久化（等待重构）

    // // sqlite - https://github.com/xerial/sqlite-jdbc
    // implementation("org.xerial:sqlite-jdbc:3.50.3.0")
    // // ktorm - https://www.ktorm.org/
    // implementation("org.ktorm:ktorm-core:3.6.0")
    // implementation("org.ktorm:ktorm-support-sqlite:3.6.0")

    // 目前仅用作参考

    // sqlite - https://github.com/xerial/sqlite-jdbc
    testImplementation("org.xerial:sqlite-jdbc:3.50.3.0")
    // ktorm - https://www.ktorm.org/
    testImplementation("org.ktorm:ktorm-core:4.1.1")
    testImplementation("org.ktorm:ktorm-support-sqlite:4.1.1")

    // byte-buddy - https://github.com/raphw/byte-buddy
    testImplementation("net.bytebuddy:byte-buddy:1.17.7")
    testImplementation("net.bytebuddy:byte-buddy-agent:1.17.7")

    // jte - https://github.com/casid/jte
    testImplementation("gg.jte:jte:3.2.1")
    testCompileOnly("gg.jte:jte-kotlin:3.2.1")

    // 用于工具代码（src/test/tool）

    // jackson-csv - https://github.com/FasterXML/jackson-dataformats-text/tree/master/csv
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.20.0") {
        exclude(group = "com.fasterxml.jackson.core")
    }
}

sourceSets {
    main {
        java.srcDirs("src/main/java", "src/main/kotlin", "src/main/gen")
        resources.srcDirs("src/main/resources")
    }
    test {
        java.srcDirs("src/test/java", "src/test/kotlin")
        java.srcDirs("src/test/tool") // tool codes (e.g., CwtConfigGenerator) (NOTE: cannot move to another source set)
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
val excludesInZip = emptyList<String>()
val cwtConfigDirs = listOf(
    "core" to "core",
    "cwtools-ck2-config" to "ck2",
    "cwtools-ck3-config" to "ck3",
    "cwtools-eu4-config" to "eu4",
    "cwtools-hoi4-config" to "hoi4",
    "cwtools-ir-config" to "ir",
    "cwtools-stellaris-config" to "stellaris",
    "cwtools-vic2-config" to "vic2",
    "cwtools-vic3-config" to "vic3",
)

tasks {
    jar {
        // 添加项目文档和许可证
        from("README.md", "README_en.md", "LICENSE")
        // 排除特定文件
        excludesInJar.forEach { exclude(it) }

        // 添加规则文件
        cwtConfigDirs.forEach { (cwtConfigDir, toDir) ->
            into("config/$toDir") {
                from("cwt/$cwtConfigDir") {
                    includeEmptyDirs = false
                    include("**/*.cwt", "**/LICENSE", "**/*.md")
                    // 打平config子目录中的文件
                    eachFile {
                        val i = path.indexOf("/config", ignoreCase = true)
                        if (i != -1) path = path.removeRange(i, i + 7)
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
        // 排除特定文件
        excludesInJar.forEach { exclude(it) }
    }
    buildPlugin {
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
    }
    test {
        if (!hasProperty("pls.include.test.ai")) {
            exclude("icu/windea/pls/ai/**")
        }
    }

    withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
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
