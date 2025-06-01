# Kotlin 学习笔记

## 概述

Kotlin 是 JetBrains 开发的现代静态类型编程语言，支持 JVM、Android、JS 和 Native 平台。具有空安全、扩展函数等特性。

**官方参考链接**：
- [Kotlin 文档](https://kotlinlang.org/docs/home.html)
- [标准库 API](https://kotlinlang.org/api/latest/jvm/stdlib/)
- [Kotlin 风格指南](https://kotlinlang.org/docs/coding-conventions.html)

**其他参考链接**：
- [Kotlin Koans 教程](https://play.kotlinlang.org/koans)
- [高级 Kotlin 模式](https://github.com/Kotlin/KEEP)

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
