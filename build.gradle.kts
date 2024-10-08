import org.jetbrains.changelog.*
import org.jetbrains.kotlin.gradle.tasks.*
import org.jetbrains.kotlin.utils.*

plugins {
	id("org.jetbrains.kotlin.jvm") version "1.9.25"
	id("org.jetbrains.intellij") version "1.17.4"
	id("org.jetbrains.grammarkit") version "2022.3.2.2"
	id("org.jetbrains.changelog") version "2.0.0"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

intellij {
	pluginName.set(providers.gradleProperty("pluginName"))
	type.set(providers.gradleProperty("intellijType"))
	version.set(providers.gradleProperty("intellijVersion"))
	
	plugins.add("com.intellij.platform.images")
	
	//optional
	plugins.add("uml")
	plugins.add("cn.yiiguxing.plugin.translate:3.5.2") //https://github.com/YiiGuxing/TranslationPlugin
	
	//reference
	//plugins.add("properties")
	//plugins.add("java")
	//plugins.add("org.jetbrains.kotlin")
    //plugins.add("markdown")
}

grammarKit {
	jflexRelease.set("1.7.0-2")
}

repositories {
	mavenCentral()
	maven("https://www.jetbrains.com/intellij-repository/releases")
}

dependencies {
	//FROM DDS4J
	implementation("ar.com.hjg:pngj:2.1.0")
	//CSV
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.17.2") {
		exclude(module = "jackson-annotations")
		exclude(module = "jackson-core")
		exclude(module = "jackson-databind")
	}
	////Sqlite
	//implementation("org.xerial:sqlite-jdbc:3.40.1.0")
	//Javassist
	implementation("org.javassist:javassist:3.30.2-GA")
	
	//JUnit
	testImplementation("junit:junit:4.13.2")
	//Sqlite
	testImplementation("org.xerial:sqlite-jdbc:3.46.0.0")
	//Byte Buddy
	testImplementation("net.bytebuddy:byte-buddy:1.14.17")
	testImplementation("net.bytebuddy:byte-buddy-agent:1.15.0")
	//Javassist
	testImplementation("org.javassist:javassist:3.30.2-GA")
	////JOGL
	//testImplementation("org.jogamp.jogl:jogl-all:2.3.2")
	////OPENRNDR
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
	jvmToolchain(17)
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
		duplicatesStrategy = DuplicatesStrategy.INCLUDE //需要加上
	}
	withType<KotlinCompile> {
		kotlinOptions {
			jvmTarget = "17"
			freeCompilerArgs = listOf(
				"-Xjvm-default=all",
				"-Xinline-classes",
				"-opt-in=kotlin.RequiresOptIn",
				"-opt-in=kotlin.ExperimentalStdlibApi",
			)
		}
	}
	withType<Test> {
		useJUnitPlatform()
		isScanForTestClasses = false
		include("**/*Test.class")
	}
	withType<Jar> {
		//排除特定文件
		excludesInJar.forEach { exclude(it) }
		//添加项目文档和许可证
		from("README.md", "README_en.md", "LICENSE")
		//添加CWT配置文件
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
	patchPluginXml {
		fun String.toChangeLogText(): String {
			val regex1 = """[-*] \[ ].*""".toRegex()
			val regex2 = """[-*] \[X].*""".toRegex(RegexOption.IGNORE_CASE)
			return lines()
				.run {
					val start = indexOfFirst { it.startsWith("## ${version.get()}") }
					val end = indexOfFirst(start + 1) { it.startsWith("## ") }.let { if(it != -1) it else size }
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
		
		sinceBuild.set(providers.gradleProperty("sinceBuild"))
		untilBuild.set(providers.gradleProperty("untilBuild"))
		pluginDescription.set(projectDir.resolve("DESCRIPTION.md").readText())
		changeNotes.set(projectDir.resolve("CHANGELOG.md").readText().toChangeLogText())
	}
	buildPlugin {
		//排除特定文件
		excludesInZip.forEach { exclude(it) }
		//重命名插件包
		archiveBaseName.set(providers.gradleProperty("pluginPackageName"))
	}
	runIde {
		systemProperty("idea.is.internal", true)
		systemProperty("pls.is.debug", true)
		jvmArgs("-Xmx4G") //自定义JVM参数
	}
	publishPlugin {
		token.set(providers.environmentVariable("IDEA_TOKEN"))
	}
}
