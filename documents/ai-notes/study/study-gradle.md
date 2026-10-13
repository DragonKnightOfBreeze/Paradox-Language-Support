# 学习笔记：Gradle

## 概述

Gradle 是基于 JVM 的构建工具，支持多项目构建、增量编译和自定义任务。使用 Groovy/Kotlin DSL 进行配置。

## 核心概念

### 构建脚本
```groovy
plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '1.9.0'
}

dependencies {
    implementation 'com.google.guava:guava:32.1.2-jre'
}
```

### 任务定义
```kotlin
tasks.register("hello") {
    doLast {
        println("Hello from Gradle")
    }
}
```

### 增量构建
- 使用 `@Input` 和 `@Output` 注解
- 自动跳过未更改任务

## 最佳实践

1. **使用构建缓存**
2. **并行执行任务**
3. **配置按需加载插件**

## Kotlin DSL 与 Version Catalogs

- **libs.versions.toml（示例）**：
```toml
[versions]
kotlin = "1.9.24"
junit = "5.10.3"

[libraries]
junit-bom = { module = "org.junit:junit-bom", version.ref = "junit" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter" }

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
```

- **build.gradle.kts 使用**：
```kotlin
plugins { alias(libs.plugins.kotlin.jvm) }

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}
```

## 配置缓存与构建缓存

- **gradle.properties**：
```properties
org.gradle.configuration-cache=true
org.gradle.caching=true
```

- **任务声明建议**：
  - 使用 `@CacheableTask`、`@Input`、`@OutputFile/Directory` 明确输入输出。
  - 避免在配置阶段读取环境/IO；使用 `Provider` API 延迟到执行阶段。

```kotlin
abstract class HelloTask : DefaultTask() {
    @get:Input
    abstract val message: Property<String>

    @TaskAction
    fun run() = println(message.get())
}

tasks.register<HelloTask>("hello") { message.set("Hello") }
```

## gradle.properties 推荐设置

```properties
org.gradle.parallel=true
org.gradle.daemon=true
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8
org.gradle.warning.mode=all
```

- Windows/CI 下注意编码与换行；在 PowerShell 中执行 Gradle Wrapper 使用 `./gradlew`（Unix Shell）或 `.\gradlew`（PowerShell）。

## 自定义插件（Kotlin）简述

```kotlin
// buildSrc/src/main/kotlin/MyPlugin.kt
class MyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register("myTask") {
            doLast { println("MyPlugin task") }
        }
    }
}
```

```kotlin
// buildSrc/build.gradle.kts
plugins { `kotlin-dsl` }
```

## 依赖锁定（Dependency Locking）

- **启用**：
```properties
dependencyVerificationMode=strict
```

- **生成/更新锁文件**：
```bash
./gradlew dependencies --write-locks
./gradlew --update-locks group:artifact
```

## JetBrains 插件相关任务（gradle-intellij-plugin / platform DSL）

- 常用任务：
  - `runIde`：启动沙盒 IDE 调试插件
  - `buildPlugin`：构建分发包 ZIP
  - `signPlugin`：使用证书/私钥签名
  - `verifyPluginSignature`：验签

- 环境变量注入（PEM 多行）与 Windows 执行：
  - PowerShell 执行 Wrapper：`.\gradlew signPlugin verifyPluginSignature -i`
  - 保证 `PRIVATE_KEY` 为 PKCS#8（含头尾与换行），证书链完整.

## 参考链接

- [Gradle 用户手册](https://docs.gradle.org/current/userguide/userguide.html)
- [DSL 参考](https://docs.gradle.org/current/dsl/)
- [插件门户](https://plugins.gradle.org/)
- [Gradle 性能优化](https://guides.gradle.org/performance/)
- [自定义插件开发](https://docs.gradle.org/current/userguide/custom_plugins.html)