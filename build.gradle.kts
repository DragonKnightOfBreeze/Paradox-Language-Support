import org.jetbrains.changelog.*
import org.jetbrains.intellij.platform.gradle.*
import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
    id("org.jetbrains.intellij.platform") version "2.5.0"
    id("org.jetbrains.grammarkit") version "2022.3.2.2"
    id("org.jetbrains.changelog") version "2.2.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

fun String.toChangeLogText(): String {
    val regex1 = """[-*] \[ ].*""".toRegex()
    val regex2 = """[-*] \[[xX]].*""".toRegex()
    val regex3 = """[-*]{3,}""".toRegex()
    return lines().asSequence()
        .dropWhile { !it.startsWith("## $version") }.drop(1)
        .takeWhile { !it.startsWith("## ") }
        .mapNotNull {
            when {
                it.contains("(HIDDEN)") -> null //hidden
                it.matches(regex1) -> null //undo
                it.matches(regex2) -> "*" + it.substring(5) //done
                it.matches(regex3) -> null //horizontal line
                else -> it
            }
        }
        .joinToString("\n").trim()
        .let { markdownToHTML(it) }
}

intellijPlatform {
    pluginConfiguration {
        id = providers.gradleProperty("pluginId")
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        description = provider { projectDir.resolve("DESCRIPTION.md").readText() }
        changeNotes = provider { projectDir.resolve("CHANGELOG.md").readText().toChangeLogText() }
        ideaVersion {
            sinceBuild = providers.gradleProperty("sinceBuild")
            untilBuild = provider { null }
        }
    }
    publishing {
        token = providers.environmentVariable("IDEA_TOKEN")
    }
}

grammarKit {
    jflexRelease = provider { "1.7.0-2" }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        val type = providers.gradleProperty("platformType")
        val version = providers.gradleProperty("platformVersion")
        create(type, version)

        testFramework(TestFrameworkType.Platform)

        bundledPlugins("com.intellij.platform.images")
        bundledPlugins("com.intellij.diagram")
        bundledPlugins("com.intellij.java")
        //bundledPlugins("org.jetbrains.kotlin")
        //bundledPlugins("org.intellij.plugins.markdown")
        //bundledPlugins("JavaScript")
        //bundledPlugins("tslint")

        //TranslationPlugin - https://github.com/YiiGuxing/TranslationPlugin
        plugin("cn.yiiguxing.plugin.translate:3.7.2")
    }

    //dds - https://github.com/iTitus/dds
    implementation("io.github.ititus:dds:3.1.0")
    implementation("io.github.ititus:ddsiio:3.1.0")
    //jackson-csv
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.17.2") {
        exclude(module = "jackson-annotations")
        exclude(module = "jackson-core")
        exclude(module = "jackson-databind")
    }
    ////sqlite
    //implementation("org.xerial:sqlite-jdbc:3.40.1.0")
    //javassist
    implementation("org.javassist:javassist:3.30.2-GA")

    //junit & opentest4j
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")

    //sqlite
    testImplementation("org.xerial:sqlite-jdbc:3.46.0.0")
    //javassist
    testImplementation("org.javassist:javassist:3.30.2-GA")
    //byte-buddy
    testImplementation("net.bytebuddy:byte-buddy:1.14.17")
    testImplementation("net.bytebuddy:byte-buddy-agent:1.15.0")
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
        freeCompilerArgs.addAll(listOf(
            "-Xjvm-default=all",
            "-Xinline-classes",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlin.ExperimentalStdlibApi",
        ))
    }
}

val excludesInJar = listOf(
    "icu/windea/pls/dev",
    "icu/windea/pls/core/data/CsvExtensions*.class",
)
val excludesInZip = listOf(
    "lib/jackson-dataformat-csv-*.jar",
)
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
        //添加项目文档和许可证
        from("README.md", "README_en.md", "LICENSE")
        //排除特定文件
        excludesInJar.forEach { exclude(it) }

        //添加规则文件
        cwtConfigDirs.forEach { (cwtConfigDir, toDir) ->
            into("config/$toDir") {
                from("cwt/$cwtConfigDir") {
                    includeEmptyDirs = false
                    include("**/*.cwt", "**/LICENSE", "**/*.md")
                    //打平config子目录中的文件
                    eachFile {
                        val i = path.indexOf("/config", ignoreCase = true)
                        if (i != -1) path = path.removeRange(i, i + 7)
                    }
                }
            }
        }
        //添加相关的文档和许可证
        into("config") {
            from("cwt/README.md", "cwt/LICENSE")
        }
    }
    instrumentedJar {
        //排除特定文件
        excludesInJar.forEach { exclude(it) }
    }
    buildPlugin {
        //排除特定文件
        excludesInZip.forEach { exclude(it) }
        //重命名插件包
        archiveBaseName = providers.gradleProperty("pluginPackageName")
    }
    runIde {
        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf(
                "-Didea.is.internal=true",
                "-Dpls.is.debug=true",
                "-Xmx4G",
            )
        }
    }
}
