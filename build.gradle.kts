import org.jetbrains.changelog.*
import org.jetbrains.kotlin.gradle.tasks.*
import org.jetbrains.kotlin.utils.*

plugins {
	id("idea")
	id("org.jetbrains.kotlin.jvm") version "1.8.10"
	id("org.jetbrains.intellij") version "1.11.0"
	id("org.jetbrains.grammarkit") version "2022.3"
	id("org.jetbrains.changelog") version "2.0.0"
}

group = "icu.windea"
version = "1.0.5"

intellij {
	pluginName.set("Paradox Language Support")
	type.set("IU")
	version.set("2023.1")
	plugins.add("com.intellij.platform.images")
	
	//optional
	plugins.add("uml")
	//optional
	plugins.add("cn.yiiguxing.plugin.translate:3.5.0") //https://github.com/YiiGuxing/TranslationPlugin
	
	//reference
	plugins.add("markdown")
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
	//Sqlite
	//implementation("org.xerial:sqlite-jdbc:3.40.1.0")
	//Byte Buddy
	//implementation("net.bytebuddy:byte-buddy:1.14.2")
	//Javassist
	implementation("org.javassist:javassist:3.29.2-GA")
	
	//Sqlite
	testImplementation("org.xerial:sqlite-jdbc:3.40.1.0")
	//Byte Buddy
	testImplementation("net.bytebuddy:byte-buddy:1.14.2")
	//Javassist
	testImplementation("org.javassist:javassist:3.29.2-GA")
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

data class CwtConfigDir(
	val from: String,
	val to: String
)

val cwtConfigDirs = listOf(
	CwtConfigDir("cwtools-ck2-config", "ck2"),
	CwtConfigDir("cwtools-ck3-config", "ck3"),
	CwtConfigDir("cwtools-eu4-config", "eu4"),
	CwtConfigDir("cwtools-hoi4-config", "hoi4"),
	CwtConfigDir("cwtools-ir-config", "ir"),
	CwtConfigDir("cwtools-stellaris-config", "stellaris"),
	CwtConfigDir("cwtools-vic2-config", "vic2"),
	CwtConfigDir("cwtools-vic2-config", "vic3")
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
	test {
		systemProperty("idea.force.use.core.classloader", true)
		useJUnitPlatform()
		isScanForTestClasses = false
		include("**/*Test.class")
	}
	jar {
		//排除特定的class文件
		exclude("icu/windea/pls/dev")
		//添加项目文档和许可证
		from("README.md", "README_en.md", "LICENSE")
		//添加CWT配置文件
		cwtConfigDirs.forEach { (cwtConfigDir, toDir) ->
			from("$rootDir/cwt/$cwtConfigDir") {
				includeEmptyDirs = false
				include("**/*.cwt", "**/LICENSE", "**/*.md")
				//打平/config子目录中的文件
				eachFile {
					val i = path.indexOf("/config", ignoreCase = true)
					if(i != -1) {
						path = path.removeRange(i, i + 7)
					}
				}
				into("config/cwt/$toDir")
			}
		}
	}
	patchPluginXml {
		sinceBuild.set("231")
		untilBuild.set("")
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
			.let {
				//将任务列表替换为无序列表
				val regex = "\\* \\[[xX ]\\]".toRegex()
				it.replace(regex, "*")
			}
			.let { markdownToHTML(it) }
		changeNotes.set(changelogText)
	}
	prepareSandbox {
		
	}
	runIde {
		systemProperty("idea.is.internal", true)
		systemProperty("pls.is.debug", true)
		jvmArgs("-Xmx4096m") //自定义JVM参数
	}
	publishPlugin {
		token.set(System.getenv("IDEA_TOKEN"))
	}
}
