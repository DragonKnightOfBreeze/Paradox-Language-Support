plugins {
	id("org.jetbrains.kotlin.jvm") version "1.6.0"
	id("org.jetbrains.intellij") version "1.5.2"
	id("org.jetbrains.grammarkit") version "2021.2.2"
}

group = "icu.windea"
version = "0.5.3"

intellij {
	version.set("2022.1")
	pluginName.set("Paradox Language Support")
}

repositories {
	maven("https://maven.aliyun.com/nexus/content/groups/public")
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-stdlib")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
}

sourceSets.main {
	java.srcDirs("src/main/java", "src/main/kotlin", "src/main/gen")
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
	publishPlugin {
		token.set(System.getenv("IDEA_TOKEN"))
	}
}