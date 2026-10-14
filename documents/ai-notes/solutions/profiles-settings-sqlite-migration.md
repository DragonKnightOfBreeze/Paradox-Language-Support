# ProfilesSettings 持久化迁移到 SQLite（Ktorm + DbBackedStateMap）（2025-08-31）

> - 模块：后端相关

- **背景**
  - 先前 `PlsProfilesSettings` 的多个 Map 字段依赖 IntelliJ `BaseState` 的 XML 持久化。为提升可维护性与一致性，迁移到 SQLite。
  - 对外 API 不变，内部以 DB 存储键值（value 为对象的 XML 文本表示）。

- **技术选型**
  - 数据库：SQLite。
  - ORM：Ktorm。
  - XML 编解码：IntelliJ `XmlSerializer` + JDOM（`SAXBuilder`/`XMLOutputter`），封装为 `XmlStateCodec`。

- **核心实现**
  - `icu.windea.pls.lang.settings.tools.ProfilesDatabase`
    - 统一管理 DB 连接；默认路径：`~/.windea/chronicle/database/profiles.db`（由 `icu.windea.pls.model.constants.PlsPaths.database` 确保目录存在）。
  - `icu.windea.pls.lang.settings.tools.DbBackedStateMap<T>`
    - 将 value 通过 `icu.windea.pls.lang.settings.tools.XmlStateCodec` 序列化为紧凑 XML 字符串；
    - `put/putAll` 写入内存缓冲，`flush()` 批量 upsert；
    - `remove/clear` 立即持久化，避免残留键；
    - `get` 惰性反序列化并缓存以优化读取；
    - 修复 `size`：使用 `keys.size` 进行统计。
  - `icu.windea.pls.lang.settings.PlsProfilesSettings`
    - 四个 Map 字段改为 `DbBackedStateMap`，并加 `@get:Transient` 禁用 IDE XML 对这些字段的持久化；
    - 在 `updateSettings()` 中调用 `flush()` 并自增修改计数，明确落盘时机。

- **测试与验证**
  - 集成测试：`icu.windea.pls.lang.settings.PlsProfilesSettingsDbIntegrationTest`
    - 使用临时 SQLite 文件验证嵌套对象 roundtrip、`remove()` 的即时持久化、以及多 Map 并存一致性；
    - 运行命令（PowerShell）：
      ```powershell
      .\gradlew test --tests icu.windea.pls.lang.settings.PlsProfilesSettingsDbIntegrationTest -i --stacktrace --no-configuration-cache
      ```

- **注意事项**
  - 目录初始化依赖 `PlsPathConstants.init()` 触发的异步初始化；IDE 启动后会确保 `~/.windea/chronicle/database/` 存在；
  - JDOM 相关 API 可能出现弃用告警，但不影响当前功能；如需可后续替换或抑制；
  - 谨慎选择 `flush()` 时机，建议通过统一入口（如 `updateSettings()`）聚合落盘，减少 IO；
  - 在 PowerShell 下执行 Gradle 命令注意前缀：`.\gradlew`。

- **迁移策略（旧 XML -> DB）建议**
  - 插件首次加载时检测历史 XML 数据并批量导入 DB；
  - 迁移完成写入幂等标记（DB meta 表或 `State` 字段），失败进行回滚并打印日志/通知；
  - 迁移流程的测试需覆盖空/部分损坏 XML 场景。

- **参考文件**
  - `src/main/kotlin/icu/windea/pls/lang/settings/tools/ProfilesDatabase.kt`
  - `src/main/kotlin/icu/windea/pls/lang/settings/tools/DbBackedStateMap.kt`
  - `src/main/kotlin/icu/windea/pls/lang/settings/tools/XmlStateCodec.kt`
  - `src/main/kotlin/icu/windea/pls/lang/settings/PlsProfilesSettings.kt`

- **参考命令**
  - 全量测试：
    ```powershell
    .\gradlew test -i --stacktrace --no-configuration-cache
    ```
