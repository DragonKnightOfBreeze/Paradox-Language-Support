import org.jetbrains.changelog.*
import org.jetbrains.kotlin.gradle.tasks.*
import org.jetbrains.kotlin.utils.*

plugins {
	id("org.jetbrains.kotlin.jvm") version "1.7.0"
	id("org.jetbrains.intellij") version "1.10.0"
	id("org.jetbrains.grammarkit") version "2021.2.2"
	id("org.jetbrains.changelog") version "2.0.0"
}

group = "icu.windea"
version = "0.7.6"

intellij {
	pluginName.set("Paradox Language Support")
	version.set("2022.3")
	
	plugins.add("com.intellij.platform.images")
	plugins.add("cn.yiiguxing.plugin.translate:3.3.2") //https://github.com/YiiGuxing/TranslationPlugin
	
	//reference
	plugins.add("markdown")
	plugins.add("properties")
	plugins.add("com.intellij.java")
	plugins.add("org.jetbrains.kotlin")
}

repositories {
	maven("https://maven.aliyun.com/nexus/content/groups/public")
	maven("https://www.jetbrains.com/intellij-repository/releases")
	mavenCentral()
}

dependencies {
	implementation("ar.com.hjg:pngj:2.1.0") //FROM DDS4J
	testImplementation("junit:junit:4.13.2")
	implementation(kotlin("stdlib-jdk8"))
}

sourceSets {
	main {
		java.srcDirs("src/main/java", "src/main/kotlin", "src/main/gen")
	}
	test {
		java.srcDirs("src/test/java", "src/test/kotlin", "src/reserved/java", "src/reserved/kotlin")
		resources.srcDirs("src/test/resources", "src/reserved/resources")
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
)

tasks {
	withType<KotlinCompile> {
		kotlinOptions {
			jvmTarget = "17"
			freeCompilerArgs = listOf("-Xjvm-default=all")
		}
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
			.let { markdownToHTML(it) }
		changeNotes.set(changelogText)
	}
	prepareSandbox {
		
	}
	runIde {
		jvmArgs("-Xmx4096m") //自定义JVM参数
	}
	publishPlugin {
		token.set(System.getenv("IDEA_TOKEN"))
	}
}
