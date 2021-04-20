plugins {
	java
	kotlin("jvm") version "1.4.30"
	id("org.jetbrains.intellij") version "0.7.2"
	id("org.jetbrains.grammarkit") version "2021.1.2"
}

group = "com.windea"
version = "0.2.1"

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
		token("perm:ZHJhZ29ua25pZ2h0b2ZicmVlemU=.OTItMzc3MQ==.FWLHSOKRYti2oDwA7UfzQF3Iy1vaIM")
	}
}