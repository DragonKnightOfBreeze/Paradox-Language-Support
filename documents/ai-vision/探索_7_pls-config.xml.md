# 探索_7_pls-config.xml

> 时间：2025-08-27 16:21（本地）
> 来源文件：`src/main/resources/META-INF/pls-config.xml`
> 目的：基于配置文件内容，梳理“配置（Config）/配置组（Config Group）”相关的 IDE 集成点，并就规则解析引擎的入口做合理推断与展望（不读取源码）。

---

## 一、设置页入口（Settings / Preferences）

- `applicationConfigurable id="pls.config" parentId="pls" bundle="messages.PlsBundle" key="settings.config" instance="icu.windea.pls.config.settings.PlsConfigSettingsConfigurable"`
  - 作用：在 PLS 的设置分组下增加“Config”页签。
  - i18n：文案来自 `messages.PlsBundle`，key 为 `settings.config`。
  - 推断：该设置页用于配置“配置组（Config Group）”相关行为，如本地路径、远程源、刷新策略、缓存等（具体项需源码或 UI 验证）。

## 二、与“配置组（Config Group）”相关的编辑器与索引集成

- `editorNotificationProvider` → `...CwtConfigGroupEditorNotificationProvider`
  - 在编辑器顶部显示与配置组相关的提示/操作（如缺失、需要刷新等）。
- `editorFloatingToolbarProvider` → `...ConfigGroupRefreshFloatingProvider`
  - 在相关文件上下文显示“刷新配置组”等浮动工具条。
- `additionalLibraryRootsProvider` → `...CwtConfigGroupLibraryProvider`
  - 将配置组目录作为“附加库根”加入工程索引，供解析/导航/引用等功能使用。
- `psi.treeChangePreprocessor` → `...CwtConfigGroupPsiTreeChangePreprocessor`
  - 监听 PSI 树变化，触发配置组相关的重建/刷新。
- `vfs.asyncListener` → `...CwtConfigFileListener`
  - 监听 VFS 事件（文件创建/修改/删除），异步处理配置文件变更。

> 结论：形成“文件系统变化 → PSI 变化 → 编辑器提示/刷新 → 库根更新”的闭环，确保配置组变化能及时反映到 IDE 行为与索引上。

## 三、动作与快捷键（Actions & Shortcuts）

- `action id="Pls.SyncConfigGroupFromRemote" class="...ConfigGroupSyncFromRemoteAction"`
  - 快捷键：`Alt+T`（默认）
  - 菜单：加入 `EditorContextBarMenu`，位于 `Pls.OpenModSettings` 之后。
  - 作用：从远端同步配置组（如拉取最新规则）。
- `group id="Pls.ConfigGroupRefreshActionGroup"`
  - `Pls.ConfigGroupRefreshAction`（`Ctrl+Shift+G`）
  - `Pls.HideConfigGroupRefreshAction`
  - 作用：显式刷新或隐藏刷新入口，面向当前上下文。

> 结论：提供“远端同步 + 本地刷新 + 显隐控制”的用户操作面板，覆盖常见维护场景。

## 四、与 README/其他语言模块的一致性

- README 强调 CWT 规则驱动。此处“Config/Config Group”相关集成，说明规则与索引来源高度模块化，支持远程拉取与本地增量刷新。
- 与 `pls-cwt.xml`/`pls-script.xml`/`pls-localisation.xml` 一致：通过 Provider 与 Listener 组合，为语言功能提供持续可用的规则与上下文数据。

## 五、推断与展望（不读源码前提）

- 规则解析引擎入口：
  - 你提示“入口在某个 Service 中且不一定在 XML 中声明”。结合本文件内容，合理推断：
    - 存在一个或多个负责“配置组生命周期”的服务（启动加载、远端同步、本地缓存、解析与索引、失效与刷新调度）。
    - 上述 Provider/Listener/Action 与该服务通过消息总线或直接 API 交互，触发刷新或拉取。
- 典型工作流（推断）：
  1) 工程打开/设置变更 → `additionalLibraryRootsProvider` 提供库根；
  2) `vfs.asyncListener` 发现规则文件更新 → 标记失效；
  3) `psi.treeChangePreprocessor` 感知 PSI 变更 → 调用服务做解析与索引；
  4) 编辑器侧通过 `editorNotificationProvider`/`editorFloatingToolbarProvider` 引导用户“刷新/同步”；
  5) 用户也可用快捷键 `Alt+T`（同步）或 `Ctrl+Shift+G`（刷新）主动操作。
- 风险与建议：
  - 需要节流/合并频繁刷新；
  - 远端失败时的回退与缓存；
  - 多工程/多配置组并存下的隔离与优先级策略；
  - 建议在文档补充“配置组”的术语定义、目录结构与远程源示例。

## 六、后续阅读建议（待你指定）

- 语言聚合与通用：`src/main/resources/META-INF/pls-lang.xml`
- 扩展点汇总：`src/main/resources/META-INF/pls-ep.xml`
- 静态检查与快速修复：`src/main/resources/META-INF/pls-inspections.xml`
