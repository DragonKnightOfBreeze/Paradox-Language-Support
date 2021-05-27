plugins {
	java
	kotlin("jvm") version "1.4.30"
	id("org.jetbrains.intellij") version "0.7.2"
	id("org.jetbrains.grammarkit") version "2021.1.2"
}

group = "icu.windea"
version = "0.3.0-preview-5"

intellij {
	version = "2021.1"
	pluginName = "Paradox Language Support"
}

buildscript{
	repositories {
		maven("https://maven.aliyun.com/nexus/content/groups/public")
		mavenCentral()
		maven("https://plugins.gradle.org/m2")
	}
}

repositories {
	maven("https://maven.aliyun.com/nexus/content/groups/public")
	mavenCentral()
	maven("https://plugins.gradle.org/m2")
}

dependencies{
	implementation(kotlin("stdlib"))
	implementation(kotlin("reflect"))
}

sourceSets {
	main {
		java.srcDir("src/main/gen")
		java.srcDir("src/main/kotlin")
	}
}

tasks {
	compileKotlin {
		kotlinOptions.jvmTarget = "1.8"
	}
	compileTestKotlin {
		kotlinOptions.jvmTarget = "1.8"
	}
	publishPlugin{
		token(System.getenv("IDEA_TOKEN"))
	}
}