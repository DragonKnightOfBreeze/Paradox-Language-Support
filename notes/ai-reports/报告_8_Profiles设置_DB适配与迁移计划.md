# 报告_8_Profiles设置_DB适配与迁移计划

## 概述（2025-08-31 13:30）
- 本会话聚焦于将 `PlsProfilesSettings` 的持久化从 XML 迁移至 SQLite，实现 DB 支持并通过集成测试。
- 采用 `Ktorm + SQLite`，以 `DbBackedStateMap` 作为 `BaseState` Map 字段的后端存储，外部 API 不变。
- 下一步建议在插件启动时进行一次性“旧 XML -> DB”的迁移，并补充幂等/回滚策略及单元/集成测试。

## 阶段性成果：SQLite 适配与集成测试通过（2025-08-31 13:30）
- 关键实现：
  - `icu.windea.pls.lang.settings.tools.ProfilesDatabase`
    - 使用 Ktorm 连接 SQLite，默认路径：`~/.pls/database/profiles.db`（由 `PlsPathConstants.database` 保证目录存在）。
  - `icu.windea.pls.lang.settings.tools.DbBackedStateMap`
    - 将 `Map<String, T>` 的值序列化为 XML 文本存入 DB；
    - `put/putAll` 写入内存缓冲，`flush()` 批量 upsert；
    - `remove/clear` 立即持久化；`get` 惰性反序列化并缓存；
    - 修复 `size` 逻辑：使用 `keys.size`；
    - 与 `PlsProfilesSettings` 的字段通过 `@get:Transient` 脱离 IDE 的 XML 持久化。
  - `icu.windea.pls.lang.settings.tools.XmlStateCodec`
    - 基于 IntelliJ `XmlSerializer` + JDOM `SAXBuilder/XMLOutputter` 的轻量 XML 序列化。
  - `icu.windea.pls.lang.settings.PlsProfilesSettings`
    - 在 `updateSettings()` 中调用 `flush()` 并自增修改计数，保证事务性落盘时机明确。
- 测试：`icu.windea.pls.lang.settings.PlsProfilesSettingsDbIntegrationTest`
  - 使用临时目录与独立 SQLite 文件验证：
    - 简单与嵌套对象的 roundtrip；
    - `remove()` 的即时持久化；
    - 多 Map 字段并存的读取一致性。
  - 运行命令（Windows/PowerShell）：
    ```powershell
    .\gradlew test --tests icu.windea.pls.lang.settings.PlsProfilesSettingsDbIntegrationTest -i --stacktrace --no-configuration-cache
    ```

## 实施细节（2025-08-31 13:30）
- 路径常量：`icu.windea.pls.model.constants.PlsPathConstants`
  - `~/.pls/database/` 由 `SmartInitializer` 在 IDE 启动时异步确保存在。
- DB 存储模型（简述）：
  - 建议表结构：`profiles_settings(key TEXT PRIMARY KEY, value TEXT NOT NULL, group TEXT NOT NULL)`；
  - 通过逻辑 group 区分 `gameSettings/modSettings/gameDescriptorSettings/projectSettings` 等 Map；
  - Value 为序列化后的 XML 字符串，统一由 `XmlStateCodec` 处理。
- 性能与一致性：
  - `flush()` 聚合写入减少 IO；
  - `remove/clear` 立即持久化避免“幽灵键”；
  - 读取端缓存命中优化高频访问。
- 兼容性：
  - UI 与调用方 API 保持不变；
  - 通过 `@get:Transient` 禁用原 XML 的字段持久化，避免双写。

## 后续计划（2025-08-31 13:30）
- 旧数据迁移（强烈建议）：
  - 第一次运行时检测并迁移旧 XML 持久化数据至 DB；
  - 迁移完成后写入 flag（例如 DB meta 表或 `State` 字段），幂等可重入；
  - 失败回滚策略与异常上报（日志/通知）。
- 补充测试：
  - `keys()/values()/entries()` 组合覆盖；
  - 并发/多次 `updateSettings()` 顺序写入的可见性验证；
  - 迁移流程的端到端测试（含空/部分损坏 XML）。
- 观测性：
  - 在 `ProfilesDatabase` 与 `DbBackedStateMap` 关键路径加调试日志（可按需通过 Registry/设置开关）。

