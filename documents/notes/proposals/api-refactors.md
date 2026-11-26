# 核心与外围 API 集中重构

> - 模块：后端 > API 重构
> - 版本：v3.1.x+
> - 状态：待规划（拆分自原 `roadmap.md`）
> - 标签：`select` / `search` / `presentation` / `diagram` / `visualization`

## 概述

对插件内部若干核心与外围 API 进行集中式重构与规范化，以提升链式调用体验、层间解耦度与后续扩展能力。

（注：同属本主题的 `ItemPresentation` 与 `kotlinx-serialization` 改造已分别在 `v3.0.3`、`v3.0.4` 完成，参见 [adr/item-presentations.md](../adr/item-presentations.md) 与 [adr/replace-jackson-with-kotlinx-serialization.md](../adr/replace-jackson-with-kotlinx-serialization.md)；数据层 `Data API` 的最新提案见 [proposals/data-api.md](data-api.md)。）

## 候选任务

- [ ] P3 考虑进一步重构 `select` API
  - 主要思路是更好的链式调用方式，以及更好的向上或向下查询方式。
- [ ] P3 考虑进一步重构 `search` 和 `searcher` 的相关 API
  - 主要思路是更简洁的调用方式，例如直接通过单次链式调用执行查询，无需额外创建和传入 `selector` 对象。
- [ ] P2 考虑规范化展示（`presentation`）的相关 API
  - 展示包括文本展示（`textPresentation`，来自本地化文本/扩展规则/resourceBundle 等）和图形展示（`graphPresentation`，来自图标/图片/切分后的图片等）。
  - 区别于“名字/`name`”，部分目标（定义/动态值等）存在“展示名字/`presentableName`”，一般来自最相关的本地化或同名本地化文本。
  - 针对展示名字、相关本地化、相关图片的不同适用目标和来源策略，考虑提供对应的 EP，相关代码主要位于 `icu.windea.pls.lang.presentation`。
  - 考虑提供统一的 manager，用于从各种目标获取展示名字、相关本地化与相关图片。
- [ ] P2 重构 `extensions.diagram`：将核心逻辑提取到语义层的 `lang.diagram`、`lang.graph` 等，`diagramProvider` 等尽可能仅保留作为门面和桥接。
- [ ] P2 考虑规范化可视化（`visualization`）的相关 API
  - 可视化指基于 HTML / Swing / WebView 的进阶图形展示，相关代码主要位于 `icu.windea.pls.lang.visualization`。
  - 考虑重构和提供 `visualizationManager` / `visualizationResolver` 及相关工具类；考虑移除已有的 `visualizationProvider` EP（改由直接新增组件类扩展）。
  - 目前仅提供基于 Swing UI 的科技卡可视化组件类；插件自身不计划直接提供更复杂的进阶可视化组件类，但未来仍可能视需要补充少量基于 Swing UI 的组件。
