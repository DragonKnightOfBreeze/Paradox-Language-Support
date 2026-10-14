# 依赖处理

> - 模块：构建

- 如果插件依赖的第三方库，同时又依赖 Kotlin 标准库（或者其他 IntelliJ Platform 已经包含的库，例如 Kotlin 协程库），通常需要显式排除，否则可能出现意外问题。
- 如果插件依赖的第三方库，同时又依赖注解库（如 `org.jspecify:jspecify`），通常可以显式排除，以减少插件包的大小。

例如：

```kts
implementation("dev.langchain4j:langchain4j:1.17.2") {
    exclude(group = "org.jspecify", module = "jspecify")
    exclude(group = "org.slf4j", module = "slf4j-api")
    exclude(group = "com.fasterxml.jackson.core")
}
```
