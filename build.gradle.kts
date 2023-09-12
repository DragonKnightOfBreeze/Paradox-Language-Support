import org.jetbrains.changelog.*
import org.jetbrains.kotlin.gradle.tasks.*
import org.jetbrains.kotlin.utils.*

plugins {
	id("idea")
	id("org.jetbrains.kotlin.jvm") version "1.8.10"
	id("org.jetbrains.intellij") version "1.15.0"
	id("org.jetbrains.grammarkit") version "2022.3.1"
	id("org.jetbrains.changelog") version "2.0.0"
}

group = "icu.windea"
version = providers.gradleProperty("pluginVersion").get()

intellij {
	pluginName.set(providers.gradleProperty("pluginName"))
	type.set(providers.gradleProperty("intellijType"))
	version.set(providers.gradleProperty("intellijVersion"))
	
	plugins.add("com.intellij.platform.images")
	
	//optional
	plugins.add("markdown")
	plugins.add("uml")
	plugins.add("cn.yiiguxing.plugin.translate:3.5.2") //https://github.com/YiiGuxing/TranslationPlugin
	
	//reference
	plugins.add("properties")
	plugins.add("java")
	plugins.add("org.jetbrains.kotlin")
}

grammarKit {
	jflexRelease.set("1.7.0-2")
}

repositories {
	maven("https://www.jetbrains.com/intellij-repository/releases")
	mavenCentral()
	//maven("https://maven.aliyun.com/nexus/content/groups/public")
}

dependencies {
	//FROM DDS4J
	implementation("ar.com.hjg:pngj:2.1.0")
	//CSV
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.14.2") {
		exclude(module = "jackson-annotations")
		exclude(module = "jackson-core")
		exclude(module = "jackson-databind")
	}
	////Sqlite
	//implementation("org.xerial:sqlite-jdbc:3.40.1.0")
	////Byte Buddy
	//implementation("net.bytebuddy:byte-buddy:1.14.2")
	//Javassist
	implementation("org.javassist:javassist:3.29.2-GA")
	
	//JUnit
	testImplementation("junit:junit:4.13.2")
	//Sqlite
	testImplementation("org.xerial:sqlite-jdbc:3.40.1.0")
	//Byte Buddy
	testImplementation("net.bytebuddy:byte-buddy:1.14.2")
	//Javassist
	testImplementation("org.javassist:javassist:3.29.2-GA")
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
	jvmToolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

val jarExclude = listOf(
	"icu/windea/pls/dev",
	"icu/windea/pls/core/data/CsvExtensions*.class",
)
val zipExclude = listOf(
	"lib/jackson-dataformat-csv-*.jar",
)
val cwtConfigDirs = listOf(
	"cwtools-ck2-config" to "ck2",
	"cwtools-ck3-config" to "ck3",
	"cwtools-eu4-config" to "eu4",
	"cwtools-hoi4-config" to "hoi4",
	"cwtools-ir-config" to "ir",
	"cwtools-stellaris-config" to "stellaris",
	"cwtools-vic2-config" to "vic2",
	"cwtools-vic2-config" to "vic3",
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
		systemProperty("idea.force.use.core.classloader", true)
		useJUnitPlatform()
		isScanForTestClasses = false
		include("**/*Test.class")
	}
	withType<Jar> {
		//排除特定文件
		jarExclude.forEach { exclude(it) }
		//添加项目文档和许可证
		from("README.md", "README_en.md", "LICENSE")
		//添加CWT配置文件
		cwtConfigDirs.forEach { (cwtConfigDir, toDir) ->
			from("$rootDir/cwt/$cwtConfigDir") {
				includeEmptyDirs = false
				include("**/*.cwt", "**/LICENSE", "**/*.md")
				//打平config子目录中的文件
				eachFile {
					val i = path.indexOf("/config", ignoreCase = true)
					if(i != -1) path = path.removeRange(i, i + 7)
				}
				into("config/$toDir")
			}
		}
	}
	patchPluginXml {
		sinceBuild.set(providers.gradleProperty("sinceBuild"))
		untilBuild.set(providers.gradleProperty("untilBuild"))
		val descriptionText = projectDir.resolve("DESCRIPTION.md").readText()
		pluginDescription.set(descriptionText)
		val changelogText = projectDir.resolve("CHANGELOG.md").readText()
			.lines()
			.run {
				val start = indexOfFirst { it.startsWith("## ${version.get()}") }
				val end = indexOfFirst(start + 1) { it.startsWith("## ") }.let { if(it != -1) it else size }
				subList(start + 1, end)
			}
			.joinToString("\n")
			//将任务列表替换为无序列表
			.let { "\\* \\[[xX ]\\]".toRegex().replace(it, "*") }
			.let { markdownToHTML(it) }
		changeNotes.set(changelogText)
	}
	buildPlugin {
		//排除特定文件
		zipExclude.forEach { exclude(it) }
		//重命名插件tar
		rename("instrumented\\-(.*\\.jar)", "$1")
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
