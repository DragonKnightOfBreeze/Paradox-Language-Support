import org.jetbrains.changelog.*
import org.jetbrains.intellij.platform.gradle.*
import org.jetbrains.kotlin.gradle.tasks.*
import org.jetbrains.kotlin.utils.*

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.2.0"
    id("org.jetbrains.grammarkit") version "2022.3.2.2"
    id("org.jetbrains.changelog") version "2.0.0"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

fun String.toChangeLogText(): String {
    val regex1 = """[-*] \[ ].*""".toRegex()
    val regex2 = """[-*] \[X].*""".toRegex(RegexOption.IGNORE_CASE)
    return lines()
        .run {
            val start = indexOfFirst { it.startsWith("## $version") }
            val end = indexOfFirst(start + 1) { it.startsWith("## ") }.let { if (it != -1) it else size }
            subList(start + 1, end)
        }
        .mapNotNull {
            when {
                it.contains("(HIDDEN)") -> null //hidden
                it.matches(regex1) -> null //undo
                it.matches(regex2) -> "*" + it.substring(5) //done
                else -> it
            }
        }
        .joinToString("\n")
        .let { markdownToHTML(it) }
}

intellijPlatform {
    pluginConfiguration {
        id.set(providers.gradleProperty("pluginId"))
        name.set(providers.gradleProperty("pluginName"))
        version.set(providers.gradleProperty("pluginVersion"))
        description.set(projectDir.resolve("DESCRIPTION.md").readText())
        changeNotes.set(projectDir.resolve("CHANGELOG.md").readText().toChangeLogText())
        ideaVersion {
            sinceBuild.set(providers.gradleProperty("sinceBuild"))
            untilBuild.set(provider { null })
        }
    }
    publishing {
        token.set(providers.environmentVariable("IDEA_TOKEN"))
    }
}

grammarKit {
    jflexRelease.set("1.7.0-2")
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
        //bundledPlugins("com.intellij.java")
        //bundledPlugins("org.jetbrains.kotlin")
        bundledPlugins("org.intellij.plugins.markdown")

        plugin("cn.yiiguxing.plugin.translate:3.5.2") //https://github.com/YiiGuxing/TranslationPlugin
    }

    //from dds4j
    implementation("ar.com.hjg:pngj:2.1.0")
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
    //byte-buddy
    testImplementation("net.bytebuddy:byte-buddy:1.14.17")
    testImplementation("net.bytebuddy:byte-buddy-agent:1.15.0")
    //javassist
    testImplementation("org.javassist:javassist:3.30.2-GA")
    ////jogl
    //testImplementation("org.jogamp.jogl:jogl-all:2.3.2")
    ////openrndr
    //testImplementation("org.openrndr:openrndr-dds:0.4.4-alpha2")
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
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs = listOf(
                "-Xjvm-default=all",
                "-Xinline-classes",
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlin.ExperimentalStdlibApi",
            )
        }
    }
    jar {
        //添加项目文档和许可证
        from("README.md", "README_en.md", "LICENSE")
        //排除特定文件
        excludesInJar.forEach { exclude(it) }
        //添加CWT规则文件
        cwtConfigDirs.forEach { (cwtConfigDir, toDir) ->
            into("config/$toDir") {
                from("$rootDir/cwt/$cwtConfigDir") {
                    includeEmptyDirs = false
                    include("**/*.cwt", "**/LICENSE", "**/*.md")
                    //打平config子目录中的文件
                    eachFile {
                        val i = path.indexOf("/config", ignoreCase = true)
                        if(i != -1) path = path.removeRange(i, i + 7)
                    }
                }
            }
        }
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
        archiveBaseName.set(providers.gradleProperty("pluginPackageName"))
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
