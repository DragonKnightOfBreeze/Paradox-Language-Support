# Profiles 命名与包结构建议（SQLite 重构相关）

> 目标：在不修改代码的前提下，提出可执行的命名规范与包结构重组建议，提升可读性与可维护性，并贴合 IntelliJ 平台插件的语境（Service / State / Settings 的用法）。

## 背景与现状梳理

- **核心参与文件/类**
  - `icu.windea.pls.lang.settings.PlsProfilesSettings`、`PlsProfilesSettingsState`
  - `icu.windea.pls.lang.settings.tools.DbBackedStateMap`
  - `icu.windea.pls.lang.settings.tools.ProfilesDatabase`
  - `icu.windea.pls.lang.settings.tools.ProfilesMigrator`
  - `icu.windea.pls.PlsFacade` 中的 `getProfilesSettings()`
  - `icu.windea.pls.model.ParadoxRootInfo`、`icu.windea.pls.model.ParadoxMetadata`
- **现状特征**
  - “Profiles” 相关代码散落在 `icu.windea.pls.lang.settings` 及其 `tools/` 下，命名更偏“Settings/Tools”，而非“Profiles/Storage”。
  - `PlsProfilesSettings` 是 `@Service(Service.Level.APP)`，但内部暴露 `state` 字段，语义更像“Store/Service 持有的状态集合”。
  - 四类持久化数据以 Map 形式存储：`gameDescriptorSettings`、`modDescriptorSettings`、`gameSettings`、`modSettings`。其中“DescriptorSettings”更像“元数据快照（Snapshot）”，而“Game/Mod Settings”是用户配置（选项、依赖）。
  - SQLite 通过 `ProfilesDatabase` 与 `DbBackedStateMap` 实现，值对象序列化由 `XmlStateCodec`（未列于本报告但在同包）完成。

## 命名与语义统一原则（IntelliJ 插件语境）

- **Service**：承载业务入口与生命周期（例如 `@Service` 的 Application 级服务）。
- **State**：用于序列化/持久化的数据对象类型（JetBrains 习惯以 `*State` 结尾）。
- **Settings**：更贴近“插件配置项”的叫法；而本场景的 Profiles 更像“域数据存储”，建议减少 “Settings” 命名以免与“用户首选项”混淆。
- **Storage/Persistence**：对持久化技术细节分层命名（如 `storage/` 或 `persistence/`）。
- **避免后端化命名**：尽量使用 `Service` / `Storage` / `State` / `Options` 等轻量词汇，避免过重的 `Repository`、`DAO` 等。

## 核心命名建议（类/接口/字段）

> 说明：以下为“建议名”，不强制一次性改完；可按优先级与影响面渐进式调整。

- **顶层服务**
  - `PlsProfilesSettings` → 建议：`PlsProfilesService`
    - 理由：`@Service` 语义明确；减少 `Settings` 歧义。
  - `PlsProfilesSettings.state` → 建议：`store` 或 `states`
    - 理由：表达“状态集合/存储容器”语义；与 IntelliJ 的 `*State` 类型区分。
  - `PlsFacade.getProfilesSettings()` → 建议：`getProfilesService()` 或 `getProfilesStore()`（保留现名一段时间并 `@Deprecated`）。

- **State 对象（名称反映职责）**
  - `ParadoxGameDescriptorSettingsState` → 建议：`ParadoxGameDescriptorSnapshotState`
  - `ParadoxModDescriptorSettingsState` → 建议：`ParadoxModDescriptorSnapshotState`
    - 理由：这是从 `descriptor`/`metadata` 派生并缓存的“快照”，而非“设置”。
  - `ParadoxGameSettingsState` → 建议：`ParadoxGameProfileState`
  - `ParadoxModSettingsState` → 建议：`ParadoxModProfileState`
    - 理由：这两者包含用户可控的选项与依赖，更像“Profile（配置档）”。
  - `ParadoxGameOrModSettingsState` → 建议：`ParadoxProfileState`
  - `ParadoxGameOrModOptionsSettingsState` → 建议：`ParadoxProfileOptionsState`
  - `ParadoxModDependencySettingsState` → 建议：`ParadoxModDependencyState`

- **State 扩展/感知接口**
  - `ParadoxGameDescriptorAwareSettingsState` → 建议：`ParadoxGameDescriptorAware`
  - `ParadoxModDescriptorAwareSettingsState` → 建议：`ParadoxModDescriptorAware`
    - 理由：接口本身并非 State；“Aware” 已表达含义，移除 `SettingsState` 以简化。

- **State 容器字段（四类 Map）**
  - `gameDescriptorSettings` → 建议：`gameDescriptorSnapshots`
  - `modDescriptorSettings` → 建议：`modDescriptorSnapshots`
  - `gameSettings` → 建议：`gameProfiles`
  - `modSettings` → 建议：`modProfiles`
  - `updateSettings()` → 建议：`flush()` 或 `save()`

- **持久化/存储层**
  - `DbBackedStateMap` → 备选：`DbStateMap` / `DbBackedMap` / `StateMapStore`
  - `ProfilesDatabase` → 备选：`ProfilesStorage` / `ProfilesSqliteStorage`（若将来有多实现）
  - `XmlStateCodec` → 备选：`StateXmlCodec` / `StateXmlSerializer`
  - 类内“类别字符串”`("gameDescriptorSettings" 等)` → 建议集中为常量或枚举（如 `ProfilesCategory`），避免写错；如需更名，考虑保留兼容映射。

## 包结构重组建议（领域 + 技术分层）

> 目标：让“profiles” 作为一等领域包出现；将持久化实现细节收敛到 `storage/` 与 `migration/` 下。

- `icu.windea.pls.profiles`（新）
  - `PlsProfilesService`（原 `PlsProfilesSettings`）
  - `ProfilesState.kt`（聚合 `*State` 数据类型）
  - `api/`（可选：若希望对外暴露明确的 Profile 读取/写入 API）
  - `storage/`
    - `DbBackedStateMap`（或重命名为 `DbStateMap`）
    - `codec/StateXmlCodec`
    - `sqlite/ProfilesDatabase`（或重命名为 `ProfilesSqliteStorage`）
  - `migration/`
    - `ProfilesMigrator`

- `icu.windea.pls.model` 保持，但可做轻量拆分（可选）
  - `icu.windea.pls.model.root.ParadoxRootInfo`
  - `icu.windea.pls.model.metadata.ParadoxMetadata`
  - 理由：按“根对象/元数据”语义划分，便于阅读；若工程已大量依赖现路径，可暂不拆分。

## 领域模型命名的进一步思考

- `ParadoxRootInfo`（封装 Game/Mod 根信息）
  - 备选命名：`ParadoxRoot` / `ParadoxRootContext` / `ParadoxRootEntity`
  - 现名通俗易懂，改名影响面极大，建议维持；如拆包，可在 `model.root` 下保留现名。
- `ParadoxMetadata`（Game/Mod 元数据）
  - 备选命名：`ParadoxRootMetadata`
  - 若仅服务于“根级对象”，加上 `Root` 更精确；但现名也已广泛使用，可仅在文档中澄清该接口语义。

## 兼容性与迁移注意事项

- **DB 表名与类别常量**：当前表名（如 `profile_game_descriptor` 等）设计良好，建议保持不变。若调整 `DbBackedStateMap` 的类别字符串（`gameDescriptorSettings` 等），应：
  - 在 `ProfilesDatabase.resolve(category)` 中保留旧值兼容映射；或
  - 一次性完成“内外一致”的重命名并编写迁移脚本（成本更高，不建议短期实施）。
- **Facade 兼容**：`PlsFacade.getProfilesSettings()` 建议保留，并标注 `@Deprecated`，内部转调新方法（如 `getProfilesService()`）。
- **增量重命名策略**：
  - 第一步：只移动包结构（`profiles/`, `profiles/storage/`, `profiles/migration/`），类名保持不变。
  - 第二步：重命名“歧义更大”的类型（如 `*DescriptorSettingsState` → `*DescriptorSnapshotState`）。
  - 第三步：重命名服务与 Facade 方法（`PlsProfilesSettings` → `PlsProfilesService`；新增 `getProfilesService()`）。
  - 每步完成后运行现有集成测试（如现有的 DB 集成测试）确保行为不变。

## 小而快的改进点

- **常量集中**：将 Map 类别字符串集中到 `ProfilesCategories` 常量或 `enum class ProfilesCategory`，并在 `ProfilesDatabase.resolve(...)` 做一次性映射。
- **方法语义**：`PlsProfilesSettingsState.updateSettings()` 更名为 `flush()`/`save()`，与 `DbBackedStateMap.flush()` 语义对齐。
- **键类型封装**（可选）：为 Map 的 key（当前是路径字符串）定义内联类 `@JvmInline value class RootPath(val path: String)`，在 API 上减少误用（如传入错误的 key）。

## 与 IntelliJ 平台约定的契合度

- **Service/State** 用法一致：`PlsProfilesService`（服务）、`*State`（可序列化的数据对象）。
- **存储实现可替换**：`profiles/storage/` 隔离持久化细节，未来如需扩展到其他存储引擎（H2/嵌入式 KV）更容易。
- **职责聚合**：`profiles/` 作为一等领域包，阅读者可快速定位 Profiles 相关功能。

## 建议的“名词对照表”（摘录）

- **类/接口**
  - `PlsProfilesSettings` → `PlsProfilesService`
  - `ParadoxGameDescriptorSettingsState` → `ParadoxGameDescriptorSnapshotState`
  - `ParadoxModDescriptorSettingsState` → `ParadoxModDescriptorSnapshotState`
  - `ParadoxGameSettingsState` → `ParadoxGameProfileState`
  - `ParadoxModSettingsState` → `ParadoxModProfileState`
  - `ParadoxGameOrModSettingsState` → `ParadoxProfileState`
  - `ParadoxGameOrModOptionsSettingsState` → `ParadoxProfileOptionsState`
  - `ParadoxModDependencySettingsState` → `ParadoxModDependencyState`
  - `ParadoxGameDescriptorAwareSettingsState` → `ParadoxGameDescriptorAware`
  - `ParadoxModDescriptorAwareSettingsState` → `ParadoxModDescriptorAware`
  - `DbBackedStateMap` → `DbStateMap`（或保留现名，移动到 `profiles/storage/`）
  - `ProfilesDatabase` → `ProfilesSqliteStorage`（或保留现名，移动到 `profiles/storage/sqlite/`）
- **字段/方法**
  - `state`（in `PlsProfilesSettings`）→ `store` / `states`
  - `gameDescriptorSettings` → `gameDescriptorSnapshots`
  - `modDescriptorSettings` → `modDescriptorSnapshots`
  - `gameSettings` → `gameProfiles`
  - `modSettings` → `modProfiles`
  - `updateSettings()` → `flush()` / `save()`
  - `PlsFacade.getProfilesSettings()` → `getProfilesService()` / `getProfilesStore()`（过渡期保留旧名）

## 实施路线建议（不改行为，仅命名/包重组）

- **阶段 A（低风险）**
  - 新建包：`icu.windea.pls.profiles`、`icu.windea.pls.profiles.storage`、`icu.windea.pls.profiles.migration`。
  - 移动类：`ProfilesMigrator`、`DbBackedStateMap`、`ProfilesDatabase`（与 `XmlStateCodec`）至上述包（不重命名）。
- **阶段 B（中风险）**
  - 重命名 State：将 `*DescriptorSettingsState` → `*DescriptorSnapshotState`；`*SettingsState`（Game/Mod）→ `*ProfileState`。
  - 同步更新 `PlsProfilesSettingsState` 中四个 Map 字段名。
- **阶段 C（中风险）**
  - `PlsProfilesSettings` → `PlsProfilesService`；`PlsFacade.getProfilesSettings()` 新增替代方法，旧方法 `@Deprecated`。
  - 保持 `ProfilesDatabase` 的 `category` 字符串不变，或在 `resolve` 处做兼容映射。

---

如需，我可以基于本报告在一个 PR 中分阶段提交包移动与重命名变更（先包移动，后重命名），并为 `PlsFacade` 增加过渡 API 与 `@Deprecated` 标注，以确保平滑迁移与最小代码扰动。
