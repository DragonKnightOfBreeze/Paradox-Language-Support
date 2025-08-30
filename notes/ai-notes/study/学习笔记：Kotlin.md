# 学习笔记：Kotlin

## 概述

Kotlin 是 JetBrains 开发的现代静态类型编程语言，支持 JVM、Android、JS 和 Native 平台。具有空安全、扩展函数等特性。

## 核心特性

### 空安全
```kotlin
val name: String? = null
val length = name?.length ?: 0
```

### 扩展函数
```kotlin
fun String.addExclamation() = "$this!"

"Hello".addExclamation() // "Hello!"
```

### 协程
```kotlin
suspend fun fetchData(): String {
    delay(1000)
    return "Data"
}
```

## 最佳实践

1. **避免过度使用扩展函数**
2. **合理使用数据类**
3. **协程结构化并发**

## Kotlin 与 Spring Boot 集成要点（JDK 21）

- **Gradle Kotlin DSL（关键片段）**：
```kotlin
plugins {
    kotlin("jvm") version "<ver>"
    kotlin("plugin.spring") version "<ver>" // 开启 Spring 相关优化（如 all-open）
    // 可选：kotlin("plugin.serialization") 用于 kotlinx.serialization
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict") // JSR-305 可空性严格模式
    }
}
```

- **JSR-305 可空性**：`-Xjsr305=strict` 可更严格地检查 Java 注解的可空性。
- **数据类与配置**：结合 `@ConfigurationProperties` 时注意默认值与可空性，避免 NPE。

## 协程与 Flow 常见模式

- **结构化并发**：优先通过 `CoroutineScope` 与 `SupervisorJob` 管理生命周期。
- **冷流到热流**：`flow {}` + `stateIn(scope)` 将冷流提升为可观测状态。

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class Repo(private val io: CoroutineDispatcher = Dispatchers.IO) {
    fun numbers(): Flow<Int> = flow {
        (1..3).forEach { n ->
            delay(100)
            emit(n)
        }
    }.flowOn(io)
}

class Vm(scope: CoroutineScope, repo: Repo) {
    val state: StateFlow<List<Int>> = repo.numbers()
        .runningFold(emptyList()) { acc, n -> acc + n }
        .stateIn(scope, SharingStarted.Lazily, emptyList())
}
```

## 测试（Kotest 与 Coroutines Test）

- **推荐**：`kotest` + `kotlinx-coroutines-test`
- **要点**：使用 `runTest`、`TestScope` 与虚拟时间（`advanceUntilIdle`）。

```kotlin
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class FlowSpec : StringSpec({
    "accumulate numbers" {
        runTest {
            val repo = Repo()
            val nums = repo.numbers().toList()
            nums shouldBe listOf(1,2,3)
        }
    }
})
```

## 序列化（kotlinx.serialization）

- **依赖**：`implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:<ver>")`
- **要点**：`@Serializable`、`Json { ignoreUnknownKeys = true }`

```kotlin
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class User(val id: Int, val name: String)

val json = Json { ignoreUnknownKeys = true }
val u = json.decodeFromString<User>(
    """{"id":1,"name":"Alice"}"""
)
```

## KSP 与 KAPT 简述

- **KSP（推荐）**：
  - 编译速度更快、内存占用更小。
  - 生态：Room、Moshi、Koin、Hilt（部分场景）等已支持。
- **KAPT**：
  - 兼容传统 Java 注解处理器，但构建速度较慢。

```kotlin
plugins { id("com.google.devtools.ksp") version "<ver>" }

dependencies {
    ksp("io.insert-koin:koin-ksp-compiler:<ver>")
    // kapt("group:artifact:<ver>") // 如需 KAPT
}
```

## 可扩展内容

- **语言进阶**：inline/value classes、reified 泛型、contracts、context receivers（K2）。
- **协程进阶**：取消与异常、超时与背压、`StateFlow/SharedFlow` 模式、结构化并发最佳实践。
- **多平台（KMP）**：源集布局、expect/actual、并发内存模型与 Native 互操作。
- **性能与内存**：对象分配与逃逸分析、集合与序列优化、编译器参数与内联策略。
- **序列化**：多态/上下文序列化、自定义序列化器、与 Ktor/Spring 集成。
- **KSP 实战**：增量处理、符号遍历模式、代码生成与测试。
- **工具链**：Detekt/Ktlint、Gradle 构建缓存与性能调优、版本对齐（BOM）。
- **与框架集成**：Ktor/Spring 中的协程上下文管理、异常映射与依赖注入。

## 参考链接

- [Kotlin 文档](https://kotlinlang.org/docs/home.html)
- [标准库 API](https://kotlinlang.org/api/latest/jvm/stdlib/)
- [Kotlin 风格指南](https://kotlinlang.org/docs/coding-conventions.html)
- [Kotlin Koans 教程](https://play.kotlinlang.org/koans)
- [高级 Kotlin 模式](https://github.com/Kotlin/KEEP)