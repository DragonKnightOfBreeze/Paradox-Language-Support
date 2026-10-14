# 将 JSON 序列化框架从 jackson 改为 kotlinx-serialization

> - 模块：构建 > 依赖
> - 版本：v3.0.4
> - 日期：2026-10-02
> - 状态：已实施

说明：
- 将 JSON 序列化框架从 jackson 改为 kotlinx-serialization（通过修改 `build.gradle.kts`）。同步修改相关的生产代码和测试代码。
- 不再显式排除 jackson 依赖（通过在 `build.gradle.kts` 中移除 `exclude(group = "com.fasterxml.jackson.core")`），以解决插件在 IDEA 2026.3 eap 中存在的兼容性问题。

关于兼容性问题：
- 平台内置的 jackson 依赖来自 `lib/lib-client.jar`，仅存在于编译/客户端侧，2026.3 EAP 已不再提供。
- 对于 2026.3 EAP，如果仍然在 `build.gradle.kts` 中显式排除 jackson 依赖，将会导致意外的报错。
- 报错信息示例：`java.lang.NoClassDefFoundError: com/fasterxml/jackson/databind/RuntimeJsonMappingException`

通过 plugin-verifier 验证兼容性：

```bash
# 默认使用 `recommended()`，这也会包括 2026.3 EAP
# 由于网络问题，难以在本地验证，考虑正式发布前再验证
.\gradlew verifyPlugin
```

用法：
- `json.encodeToString(...)` - 写入（序列化/编码）到目标字符串。
- `json.encodeToStream(...)` - 写入（序列化/编码）到目标流（`OutputStream`）。注意这并不会自动关闭流，因此通常需要结合使用 `outputStream.use {...}`。
- `json.decodeFromString(...)` - 读取（反序列化/解码）自目标字符串。
- `json.decodeFromStream(...)` - 读取（反序列化/解码）自目标流（`InputStream`）。注意这并不会自动关闭流，因此通常需要结合使用 `inputStream.use {...}`。
- 对于 `path.outputStream()`，默认使用选项 `CREATE` `TRUNCATE_EXISTING` `WRITE`，因此如果文件未创建则先创建，如果已创建则会截断其内容。

任务：
- [x] 初步的迁移工作（借助AI）
- [x] 补充更多冒烟测试 （借助AI）
- [x] 后续清理（例如，重构和优化 `JsonService` `MarkdownService` `ChronicleJsonService`）

关键代码位置：
- [x] `icu.windea.pls.core.data.JsonService`
- [x] `icu.windea.pls.base.data.ChronicleJsonService`
