# 模组导入与导出设计规范

> - 版本：`v3.1.0`
> - 更新日期：`2026-11-28`
> - 状态：**现行活规范（Living Specification）**
> - 文档定位：规范模组依赖导入导出子系统的扩展点、启动器格式探测、行为约束与路径/JSON 兼容边界。

---

## 概述

目前用于：

- 在游戏或模组设置的对话框中，从各种数据文件导入模组信息到模组依赖列表中。
- 在游戏或模组设置的对话框中，从模组依赖列表导出模组信息到各种数据文件。

IMM 参考链接：

- https://github.com/bcssov/IronyModManager
- https://github.com/bcssov/IronyModManager/tree/master/src/IronyModManager.IO/Mods/Importers
- https://github.com/bcssov/IronyModManager/tree/master/src/IronyModManager.IO/Mods/Exporter

## 核心组件与扩展点

核心代码：

- `icu.windea.pls.ep.tools` - EP 与模型类所在的包
- `icu.windea.pls.ep.tools.importer.ParadoxModImporter` - 模组导入器 EP 接口（`icu.windea.pls.modImporter`）
- `icu.windea.pls.ep.tools.exporter.ParadoxModExporter` - 模组导出器 EP 接口（`icu.windea.pls.modExporter`）
- `icu.windea.pls.lang.ui.settings.ParadoxModDependenciesImportPopup` - 导入模组依赖的弹出窗口 UI
- `icu.windea.pls.lang.ui.settings.ParadoxModDependenciesExportPopup` - 导出模组依赖的弹出窗口 UI
- `icu.windea.pls.lang.tools.SpecialPathService` - 定位游戏数据目录、Steam 创意工坊目录与默认 `playlists` 目录

模组导入器实现：

- `icu.windea.pls.ep.tools.importer.ParadoxJsonBasedModImporter` - 基于 JSON 的模组导入器，始终可用
  - [x] `icu.windea.pls.ep.tools.importer.ParadoxGameJsonImporter` - 基于 `dlc_load.json` 或 `content_load.json`
  - [x] `icu.windea.pls.ep.tools.importer.ParadoxLauncherJsonImporter` - 基于 `playlist.json`，统一导入器 + 自动探测 V2/V3
- `icu.windea.pls.ep.tools.importer.ParadoxDbBasedModImporter` - 基于 SQLite 的模组导入器，受能力门控
  - [x] `icu.windea.pls.ep.tools.importer.ParadoxLauncherDbImporter` - 基于 `launcher-v2.sqlite`，版本：V2，V4，V5
  - [x] `icu.windea.pls.ep.tools.importer.ParadoxLauncherBetaDbImporter` - 基于 `launcher-v2_openbeta.sqlite`，版本：V2，V4，V5

模组导出器实现：

- `icu.windea.pls.ep.tools.exporter.ParadoxJsonBasedModExporter` - 基于 JSON 的模组导出器，始终可用
  - [x] `icu.windea.pls.ep.tools.exporter.ParadoxGameJsonExporter` - 基于 `dlc_load.json` 或 `content_load.json`
  - [x] `icu.windea.pls.ep.tools.exporter.ParadoxLauncherJsonV2Exporter` - 基于 `playlist.json`，版本：V2（< 2021.10）
  - [x] `icu.windea.pls.ep.tools.exporter.ParadoxLauncherJsonV3Exporter` - 基于 `playlist.json`，版本：V3（≥ 2021.10）
- `icu.windea.pls.ep.tools.exporter.ParadoxDbBasedModExporter` - 基于 SQLite 的模组导出器，受能力门控
  - [x] `icu.windea.pls.ep.tools.exporter.ParadoxLauncherDbExporter` - 基于 `launcher-v2.sqlite`，版本：V2，V4，V5
  - [x] `icu.windea.pls.ep.tools.exporter.ParadoxLauncherBetaDbExporter` - 基于 `launcher-v2_openbeta.sqlite`，版本：V2，V4，V5

## 数据格式与版本探测

- `ParadoxGameJsonImporter` / `ParadoxGameJsonExporter` 按 `ParadoxGameTypeConstraint.DescriptorModUsed` 分流：使用 `descriptor.mod` 的游戏读写 `dlc_load.json`，其余游戏（`MetadataJsonUsed`，即 VIC3 / EU5）读写 `content_load.json`
- `ParadoxLauncherJsonImporter` 通过 `ParadoxMetadataUtil.detectLauncherPlaylistPositionIsInt` 探测 `mods[].position` 类型：整数按 V3 解析，字符串按 V2 解析，无法探测时先尝试 V2 再回退 V3
- V2 的 `position` 为字符串，排序时统一解析为数值（十进制或十六进制均可）；V4+ 的 `position` 为整数，从 0 开始
- 启动器 JSON 导出仍按 V2 / V3 拆分为两个导出器，分别写入对应格式的 `playlist.json`
- 启动器数据库导出通过 `knex_migrations` 中的 `modifyPositionToInteger` 判断 V4+，再决定 `position` 的写入格式；数据库导入则统一按字符串读取并解析排序

## 行为约束

- 导出仅处理启用的模组；官方启动器 JSON 与数据库导出会排除 `ParadoxModSource.Local` 的本地模组，仅保留 Steam / Paradox Mods 远端 ID
- 数据库导出写入当前已激活的播放集，或同名播放集（不存在时创建且默认未激活）
- 导入会跳过已存在的模组目录，结果通过 `ParadoxModImporter.Result(total, actualTotal, newModSetInfo, warning)` 返回
- 导入/导出弹窗按结果发送通知：空结果或 `warning` 使用警告级别，成功使用信息级别
- `ParadoxDbBasedModImporter` / `ParadoxDbBasedModExporter` 的 `isAvailable` 受 `ChronicleCapabilities.General.includeSqlite` 门控；JSON 导入器/导出器始终可用

## 路径定位与 JSON 兼容

- `SpecialPathService` 负责定位游戏数据目录、Steam 游戏创意工坊目录与默认 `playlists` 目录，供各导入器/导出器选择默认文件
- JSON 导入/导出统一使用 `JsonService.read` / `JsonService.write`：读取默认跳过 UTF-8 BOM（`JsonOptions.skipUtf8Bom = true`），写入统一为不含 BOM 的 UTF-8
