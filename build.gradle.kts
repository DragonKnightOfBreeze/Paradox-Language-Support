import org.jetbrains.changelog.*
import org.jetbrains.intellij.platform.gradle.*
import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.10" // https://kotlinlang.org/docs/gradle.html
    id("org.jetbrains.intellij.platform") version "2.7.2" // https://github.com/JetBrains/intellij-platform-gradle-plugin
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
    intellijPlatform {
        val type = properties("platformType")
        val version = properties("platformVersion")
        create(type, version) // https://github.com/JetBrains/intellij-platform-plugin

        testFramework(TestFrameworkType.Platform)

        bundledPlugins("com.intellij.platform.images")
        bundledPlugins("org.intellij.plugins.markdown")
        bundledPlugins("com.intellij.diagram")

        // 用作参考
        // bundledPlugins("com.intellij.java")
        // bundledPlugins("org.jetbrains.kotlin")
        // bundledPlugins("JavaScript")
        // bundledPlugins("tslint")

        // TranslationPlugin - https://github.com/YiiGuxing/TranslationPlugin
        plugin("cn.yiiguxing.plugin.translate:3.7.2")
    }

    // junit - https://github.com/junit-team/junit4
    testImplementation("junit:junit:4.13.2")
    // opentest4j - https://github.com/ota4j-team/opentest4j
    testImplementation("org.opentest4j:opentest4j:1.3.0")

    // TwelveMonkeys - https://github.com/haraldk/TwelveMonkeys
    implementation("com.twelvemonkeys.imageio:imageio-dds:3.12.0")
    implementation("com.twelvemonkeys.imageio:imageio-tga:3.12.0")

    // dds - https://github.com/iTitus/dds
    implementation("io.github.ititus:dds:3.1.0")
    // implementation("io.github.ititus:ddsiio:3.1.0")

    // javassist - https://github.com/jboss-javassist/javassist
    implementation("org.javassist:javassist:3.30.2-GA")

    // jackson-csv - https://github.com/FasterXML/jackson-dataformats-text/tree/master/csv
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.19.2") {
        exclude(group = "com.fasterxml.jackson.core")
    }

    // pebble - https://github.com/PebbleTemplates/pebble
    implementation("io.pebbletemplates:pebble:3.2.4")

    // AI 集成

    // LangChain4J - https://github.com/langchain4j/langchain4j
    implementation("dev.langchain4j:langchain4j:1.3.0") {
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("dev.langchain4j:langchain4j-open-ai:1.3.0") {
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("dev.langchain4j:langchain4j-kotlin:1.3.0-beta9") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "com.fasterxml.jackson.core")
    }

    // 目前仅用作参考

    // sqlite - https://github.com/xerial/sqlite-jdbc
    testImplementation("org.xerial:sqlite-jdbc:3.46.0.0")

    // byte-buddy - https://github.com/raphw/byte-buddy
    testImplementation("net.bytebuddy:byte-buddy:1.14.17")
    testImplementation("net.bytebuddy:byte-buddy-agent:1.17.7")

    // jte - https://github.com/casid/jte
    testImplementation("gg.jte:jte:3.2.1")
    testCompileOnly("gg.jte:jte-kotlin:3.2.1")
}

sourceSets {
    main {
        java.srcDirs("src/main/java", "src/main/kotlin", "src/main/gen")
        resources.srcDirs("src/main/resources")
    }
    test {
        java.srcDirs("src/test/java", "src/test/kotlin")
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
    publishing {
        token = envVars("IDEA_TOKEN")
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    header = version
    headerParserRegex.set("""[a-zA-Z0-9.-]+""".toRegex())
    groups.empty()
    keepUnreleasedSection = true
    repositoryUrl = properties("pluginRepositoryUrl")
}

grammarKit {
    jflexRelease = "1.7.0-2"
}

val excludesInJar = listOf(
    "icu/windea/pls/dev",
)
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
    withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
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
        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf(
                "-Didea.is.internal=true",
                "-Dpls.is.debug=true",
            )
        }
    }
}
