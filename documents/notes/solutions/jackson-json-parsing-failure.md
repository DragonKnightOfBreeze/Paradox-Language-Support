# jackson 解析 json 失败的问题

> 所属类别：`后端`
> 上级导航：[问题解决与避坑索引 (Solutions Index)](README.md)

在单元测试通过的情况下，在调试 IDE 时 json 解析失败，而之前并非出现过这种情况

build.gradle.kts 中并未显式声明（且尽可能排除了） jackson 相关的依赖

`ObjectMappers.jsonMapper` 的定义：

```
val jsonMapper by lazy {
    jacksonObjectMapper().apply {
        findAndRegisterModules()
        disable(SerializationFeature.INDENT_OUTPUT)
        enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
        enable(JsonParser.Feature.IGNORE_UNDEFINED)
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }
}
```

如果仅在 build.gradle.kts 中显式加上以下依赖，且去除 `findAndRegisterModules()`，则可以解决问题。

```
implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")
implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.0")
implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.10")
```

如果仅在 ktorm 依赖中排除 kotlin stdlib 的依赖，则可以解决问题。

```
implementation("org.ktorm:ktorm-core:4.1.1") {
    exclude(group = "org.jetbrains.kotlin")
}
implementation("org.ktorm:ktorm-support-sqlite:4.1.1") {
    exclude(group = "org.jetbrains.kotlin")
}
```

**结论**： 如果要导入额外的依赖，需要尽可能地排除 IntelliJ 中已包含的依赖，例如 kotlin stdlib 和 jackson。
