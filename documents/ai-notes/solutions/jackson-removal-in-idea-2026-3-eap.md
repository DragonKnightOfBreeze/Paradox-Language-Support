# 平台不再提供 Jackson 导致 IDEA 2026.3 EAP 兼容性问题（迁移到 kotlinx-serialization）（2026-09-30）

> - 模块：前端相关（参考文档）

> 备注（2026-10-01）：可以通过配置 `allowComments = true` 以允许注释。

- **背景**
  - 在 IDEA 2026.3 EAP 中，插件报错 `java.lang.NoClassDefFoundError: com/fasterxml/jackson/databind/RuntimeJsonMappingException`。
- **原因**
  - 项目从未显式声明 Jackson 依赖（`dependencyInsight` 在 compile/runtime 配置中均查不到 `jackson-databind` / `jackson-core` / `jackson-annotations`）。
  - Jackson 的类实际来自平台自带的 `lib/lib-client.jar`（如 `ideaIU-2025.2-win/lib/lib-client.jar` 内含 `jackson-databind 2.20.2`）；该 jar 仅存在于编译/客户端侧，2026.3 EAP 已不再提供，导致运行期类缺失。
  - 同时，langchain4j 的 Jackson 传递依赖此前被显式排除，其运行同样依赖平台 Jackson，存在相同风险。
- **解决方案**
  - 将插件自身的 JSON 序列化框架从 Jackson 迁移到 kotlinx-serialization：
    - 引入 `org.jetbrains.kotlin.plugin.serialization`（版本与 Kotlin 对齐）与 `org.jetbrains.kotlinx:kotlinx-serialization-json`。
    - `JsonService` 改为提供 kotlinx `Json` 实例；`Any` 字段改用 `JsonElement`；`readTree` 改用 `parseToJsonElement`。
    - 模型/枚举改为 `@Serializable`，snake_case 字段用 `@SerialName`；`ParadoxGameType` 的 id 映射通过枚举项 `@SerialName` 实现（删除原 `JsonModuleWithType` / `ChronicleJsonModules`）。
    - 打包资源 `/data/*.json5` 保留 JSON5 写法，读取前用 `stripJson5Comments` 去除注释（kotlinx 不支持注释），并配合 `isLenient = true` 与 `allowTrailingComma = true`。
    - 移除 langchain4j 的 `exclude(group = "com.fasterxml.jackson.core")`，让其自带 Jackson，避免再次依赖平台。
  - 依赖策略：显式引入 kotlinx-serialization，不特殊处理 Jackson（不显式排除）；并在最低支持版本（2025.2）与 2026.3 EAP 上分别验证。
- **注意事项**
  - kotlinx-serialization 对缺失键比 Jackson 严格：无默认值的可空字段必须补 `= null`，否则真实文件可能解析失败。
  - 输出行为尽量对齐 Jackson：`prettyPrint = true`（2 空格缩进）、`encodeDefaults = true`、`explicitNulls = false`。
- **参考链接**
  - kotlinx.serialization：https://github.com/Kotlin/kotlinx.serialization
