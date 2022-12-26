import org.jetbrains.changelog.*
import org.jetbrains.kotlin.gradle.tasks.*
import org.jetbrains.kotlin.utils.*

plugins {
	id("org.jetbrains.kotlin.jvm") version "1.7.22"
	id("org.jetbrains.intellij") version "1.11.0"
	id("org.jetbrains.grammarkit") version "2022.3"
	id("org.jetbrains.changelog") version "2.0.0"
}

group = "icu.windea"
version = "0.7.9"

intellij {
	pluginName.set("Paradox Language Support")
	version.set("2022.3")
	
	plugins.add("com.intellij.platform.images")
	plugins.add("cn.yiiguxing.plugin.translate:3.4.0") //https://github.com/YiiGuxing/TranslationPlugin
	
	//reference
	plugins.add("markdown")
	plugins.add("properties")
	plugins.add("com.intellij.java")
	plugins.add("org.jetbrains.kotlin")
}

grammarKit {
	jflexRelease.set("1.7.0-2")
}

repositories {
	//maven("https://maven.aliyun.com/nexus/content/groups/public")
	mavenCentral()
	maven("https://www.jetbrains.com/intellij-repository/releases")
}

dependencies {
	//FROM DDS4J
	implementation("ar.com.hjg:pngj:2.1.0")
	//CSV
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.14.0") {
		exclude(module = "jackson-annotations")
		exclude(module = "jackson-core")
		exclude(module = "jackson-databind")
	}
	//JUNIT
	testImplementation("junit:junit:4.13.2")
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
	withType<KotlinCompile> {
		kotlinOptions {
			jvmTarget = "17"
			freeCompilerArgs = listOf("-Xjvm-default=all")
		}
	}
	test {
		useJUnitPlatform()
	}
	jar {
		//添加项目文档和许可证
		from("README.md", "README_en.md", "LICENSE")
		//添加CWT配置文件
		cwtConfigDirs.forEach { (cwtConfigDir, toDir) ->
			from("$rootDir/cwt/$cwtConfigDir") {
				includeEmptyDirs = false
				exclude(".*", "*.fsx", "gitlab-ci.yml")
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
		sinceBuild.set("223")
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
			.let { it.replace("* [ ]", "*") } //不保留任务列表
			.let { markdownToHTML(it) }
		changeNotes.set(changelogText)
	}
	prepareSandbox {
		
	}
	runIde {
		systemProperties["idea.is.internal"] = true
		jvmArgs("-Xmx4096m") //自定义JVM参数
	}
	publishPlugin {
		token.set(System.getenv("IDEA_TOKEN"))
	}
}
