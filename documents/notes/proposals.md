# 架构提案与预研蓝图

## 概述

本目录属于**探索沙盒与活蓝图（Living Proposals）**，存放尚未最终定稿、尚未进入生产实施、或尚在跨版本演进中的架构提案与专题设计草案。

### Proposals vs ADR 信用隔离与流转规则

- **信用隔离**：`proposals/` 中的文档允许由 AI 辅助起草并随技术预研持续修订，属于低信用候选项；`adr/` 中的文档仅记录**已决并已实施（Accepted / Completed）**的架构决策，一经实施即封存为不可变历史基线。
- **聚焦型提案**：一旦方案实施落地，应经人工/测试验证后将文档内容精炼定稿，并**晋升迁入 `adr/`**（同步更新索引）。
- **大型跨版本蓝图**：涉及多个里程碑的大体量规划继续留驻 `proposals/`，在推进过程中按实际落地的子课题**沿途孵化并沉淀独立的子 `adr/`**（例如 [complex-expression-refactors.md](proposals/complex-expression-refactors.md) 已孵化出 [adr/localisation-command-lexical-defense.md](adr/localisation-command-lexical-defense.md)）。

## 目录索引

### 后端（语法与语言基础设施）

- [次要语法与语言基础设施完善](proposals/minor-syntax-enhancement.md)
- [脚本高级插值语法研究与重构](proposals/script-syntax-advanced-interpolations.md)
- [本地化高级富文本语法研究与重构](proposals/localisation-syntax-advanced-rich-text.md)

### 后端（索引与规则系统）

- [为合并索引引入线程本地的上下文](proposals/merged-index-thread-context.md)
- [支持匹配带前缀标签的脚本表达式](proposals/prefix-tag-match.md)
- [规则系统关联 QoL 增强特性](proposals/config-related-qol-features.md)

### 后端（语义、表达式与 API 重构）

- [引入 `ParadoxData` 和 `ParadoxDataSource`（Data API）](proposals/data-api.md)
- [核心与外围 API 集中重构（`select` / `search` / `presentation` / `diagram` / `visualization`）](proposals/api-refactors.md)
- [复杂表达式 API 集中重构](proposals/complex-expression-refactors.md)
- [`[VIC3/CK3]` 本地化命令的高级复杂表达式与单引号字面量语义支持](proposals/localisation-command-advanced-semantic-support.md)

### 后端（语言构造与语言功能）

- [支持内联定义](proposals/inline-definition.md)
- [宏展开与求值支持](proposals/macro-explanation.md)
