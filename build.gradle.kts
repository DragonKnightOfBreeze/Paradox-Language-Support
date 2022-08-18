plugins {
	id("org.jetbrains.kotlin.jvm") version "1.7.0"
	id("org.jetbrains.intellij") version "1.8.0"
	id("org.jetbrains.grammarkit") version "2021.2.2"
}

group = "icu.windea"
version = "0.7.0"

intellij {
	version.set("2022.2")
	pluginName.set("Paradox Language Support")
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
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-stdlib")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("ar.com.hjg:pngj:2.1.0") //FROM DDS4
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

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(11))
	}
}

kotlin {
	jvmToolchain {
		this as JavaToolchainSpec
		languageVersion.set(JavaLanguageVersion.of(11))
	}
}

val projectCompiler = javaToolchains.compilerFor {
	languageVersion.set(JavaLanguageVersion.of(11))
}

data class CwtConfigDir(
	val from: String,
	val to: String,
	val flatConfigDir: Boolean = false
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
	jar {
		//添加项目文档和许可证
		from("README.md", "README_en.md", "LICENSE")
		//添加CWT配置文件
		cwtConfigDirs.forEach { (cwtConfigDir, toDir) ->
			from("$rootDir/cwt/$cwtConfigDir") {
				includeEmptyDirs = false
				exclude("**/.*")
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
	compileJava {
		javaCompiler.set(projectCompiler)
	}
	compileTestJava {
		javaCompiler.set(projectCompiler)
	}
	compileKotlin {
		kotlinOptions {
			jvmTarget = "11"
			freeCompilerArgs = listOf("-Xjvm-default=all")
		}
	}
	compileTestKotlin {
		kotlinOptions {
			jvmTarget = "11"
			freeCompilerArgs = listOf("-Xjvm-default=all")
		}
	}
	buildSearchableOptions {
		enabled = false
	}
	prepareSandbox {
		
	}
	publishPlugin {
		token.set(System.getenv("IDEA_TOKEN"))
	}
	runIde {
		//添加自定义配置项到systemProperties中（idea.properties中的配置项会被识别为systemProperties）
		//通过调用java.lang.Boolean.getBoolean()来检查配置项
		systemProperty("pls.debug", true)
		//自定义JVM参数
		jvmArgs("-Xmx4096m")
	}
}