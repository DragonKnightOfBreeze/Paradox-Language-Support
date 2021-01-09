plugins {
	java
	kotlin("jvm") version "1.4.0"
	id("org.jetbrains.intellij") version "0.4.21"
	id("org.jetbrains.grammarkit") version "2020.1.2"
}

group = "com.windea"
version = "0.1"

intellij {
	version = "2020.3"
	pluginName = "Paradox Language Support"
}

buildscript {
	repositories {
		maven("https://maven.aliyun.com/nexus/content/groups/public")
		mavenCentral()
		jcenter()
	}
}

repositories {
	maven("https://dl.bintray.com/kotlin/kotlin-eap")
	maven("https://maven.aliyun.com/nexus/content/groups/public")
	mavenCentral()
	jcenter()
}

dependencies{
	implementation(kotlin("stdlib"))
	implementation(kotlin("reflect:1.4.0"))
}

sourceSets {
		main {
		java.srcDir("src/main/gen")
		java.srcDir("src/main/kotlin")
	}
}

tasks {
	compileKotlin {
		incremental = true
		//javaPackagePrefix = "com.windea.plugin.idea"
		kotlinOptions.jvmTarget = "11"
	}
	compileTestKotlin {
		incremental = true
		//javaPackagePrefix = "com.windea.plugin.idea"
		kotlinOptions.jvmTarget = "11"
	}
}

grammarKit {
	jflexRelease = "1.7.0-1"
	grammarKitRelease = "2020.1"
}
