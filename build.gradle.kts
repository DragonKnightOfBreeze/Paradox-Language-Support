plugins {
	id("org.jetbrains.kotlin.jvm") version "1.6.0"
	id("org.jetbrains.intellij") version "1.5.2"
	id("org.jetbrains.grammarkit") version "2021.2.2"
}

group = "icu.windea"
version = "0.6.1"

intellij {
	version.set("2022.1")
	pluginName.set("Paradox Language Support")
	plugins.add("com.intellij.platform.images")
	
	//reference
	plugins.add("markdown")
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

tasks {
	jar {
		from("README.md", "README_en.md", "LICENSE")
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
	//buildSearchableOptions {
	//	enabled = false
	//}
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