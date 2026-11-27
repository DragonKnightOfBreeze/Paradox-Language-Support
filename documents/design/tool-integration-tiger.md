# 外部检查工具集成：Tiger

> - 版本：`v3.1.0`
> - 更新日期：`2026-11-28`
> - 状态：**现行活规范（Living Specification）**
> - 文档定位：规范 `*-tiger` 检查工具在插件中的 provider 注册、设置 UI、命令执行、结果映射、快速修复与缓存失效机制。

---

## 概述

插件通过统一的 `LintToolProvider` / `LintRequest` / `LintResult` 抽象集成 Tiger 系列检查工具，并将简化后的 JSON 结果映射为代码检查。

- 当前注册并暴露配置的 provider 共 5 个：`ck3-tiger`、`eu5-tiger`、`hoi4-tiger`、`imperator-tiger`、`vic3-tiger`。
- 检查仅作用于 `ParadoxRootInfo.Mod` 模组根目录中的文件。
- 配置入口是单一 Lint 设置分组，另提供模组/游戏级 `disableTiger` 开关与 `TigerHighlightDialog` 高亮映射对话框。
- 执行工具并读取 JSON 输出，将结果按文件路径分组后用于代码检查；快速修复目前仅覆盖 wiki 链接。

## 工具矩阵与注册

| Provider | 名称 | 对应游戏 |
| :--- | :--- | :--- |
| `TigerLintToolProvider.Ck3` | `ck3-tiger` | CK3 |
| `TigerLintToolProvider.Eu5` | `eu5-tiger` | EU5 |
| `TigerLintToolProvider.Hoi4` | `hoi4-tiger` | HOI4 |
| `TigerLintToolProvider.Ir` | `imperator-tiger` | Imperator: Rome |
| `TigerLintToolProvider.Vic3` | `vic3-tiger` | VIC3 |

- 5 个 provider 均注册到 `icu.windea.pls.integrations.lintToolProvider` EP。
- `TigerLintToolProvider` 派生自 `CommandLineBasedLintToolProvider`，按 `forGameType` 匹配游戏，并按名称与常规文件校验可执行文件（不通过执行命令行探测）。
- `ChronicleIntegrationSettingsConfigurable.getTigerSettingsMap` 为上述 5 个 provider 暴露可执行文件路径与 `.conf` 路径配置项。

## 配置 UI

- 图片、翻译、Lint 各自一个设置分组；Lint 分组内先提供全局 `enableTiger` 复选框，再平铺各 provider 的可执行文件路径与 `.conf` 路径输入行。
- 模组/游戏级别另提供 `disableTiger` 开关，命中时跳过该根目录的 Tiger 检查。
- `TigerHighlightDialog` 提供“严重度 × 置信度”高亮级别映射，父级在子项不一致时显示混合级别；点击确定后保存设置并使相关缓存失效。
- 修改路径或高亮映射后，通过 `onTigerSettingsChanged` 刷新 Inlay Hints 并递增对应游戏类型的修改追踪计数；切换 `enableTiger` 仅刷新 Inlay Hints。

## 执行与参数

- `TigerLintToolService.collectRequest` 仅对 `ParadoxLanguage` 文件生效，要求根目录为 `ParadoxRootInfo.Mod`、工具可用且已配置可执行文件路径，并检查 `disableTiger`。
- 工作目录为可执行文件所在目录；`modPath` 优先取 `.mod` 文件路径，否则取模组根目录；`gamePath` 来自模组设置的最终游戏目录。
- 命令行形式为 `<executable> --json [--config <confPath>] [--game <gamePath>] <modPath>`，统一使用 UTF-8。
- 检查结果同时写入 `ChronicleDataPathService.lintResultsPath` 下的 `<tool>-result@<uuid>.json`，并立即解析为 `TigerLintResult`。
- 执行使用长超时；被取消、超时或非零退出码均抛出异常。若在 EDT 或 read action 中调用，会记录警告；严格测试模式下直接抛出异常。

## 结果模型与代码检查

- `TigerLintResult.Item` 由 `severity`（`tips` / `untidy` / `warning` / `error` / `fatal`）与 `confidence`（`weak` / `reasonable` / `strong`）共同描述，并携带 `key`、`message`、`info`、`wiki` 与 `locations`。
- `TigerLintResult.itemGroups` 按文件路径分组，`fromPath` 供注解器提取当前文件的条目。
- `ChronicleTigerLintAnnotator`（`ExternalAnnotator`）与 `ChronicleTigerLintInspection`（`LocalInspectionTool` + `ExternalAnnotatorBatchInspection`）配对注册于 `PARADOX` 语言。
- 严重度映射默认按 `TigerLintToolUtil.getDefaultHighlightSeverity` 计算，并可由 `TigerHighlightDialog` 的配置覆盖。
- 快速修复只有针对 `item.wiki` 的 `OpenUrlFix`，暂不提供通用修复。

## 缓存与失效

- 检查结果通过 `TrackingCache` 缓存，配置为 `maximumSize=128`、`expireAfterAccess=30m`、`softValues`。
- 缓存条目由 `rootFile`、游戏类型修改追踪器、`ScriptFile` 与 `LocalisationFile` 组成的 `MergedModificationTracker` 追踪。
- 相同请求通过请求级 `synchronized` 锁合并，避免重复执行；失败状态按请求记录，用于通知判定。
- `TigerConfFileListener` 作为异步 VFS 监听器监听 `.conf` 文件变更，命中已启用工具的配置文件名后递增对应游戏类型的修改追踪计数。
- 设置页面修改路径或高亮映射后，同样通过修改追踪计数使缓存失效。

## 参考链接

- [Issue #128: [VIC3/CK3] Add ck3-tiger validator support to Plugin](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/128)
- [amtep/tiger (GitHub)](https://github.com/amtep/ck3-tiger)
- [palantir/tslint (GitHub)](https://github.com/palantir/tslint)
- [JetBrains/intellij-plugins/tslint](https://github.com/JetBrains/intellij-plugins/tree/master/tslint)
