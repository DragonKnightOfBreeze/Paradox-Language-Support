# Gradle 学习笔记

## 概述

Gradle 是基于 JVM 的构建工具，支持多项目构建、增量编译和自定义任务。使用 Groovy/Kotlin DSL 进行配置。

**官方参考链接**：
- [Gradle 用户手册](https://docs.gradle.org/current/userguide/userguide.html)
- [DSL 参考](https://docs.gradle.org/current/dsl/)
- [插件门户](https://plugins.gradle.org/)

**其他参考链接**：
- [Gradle 性能优化](https://guides.gradle.org/performance/)
- [自定义插件开发](https://docs.gradle.org/current/userguide/custom_plugins.html)

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
