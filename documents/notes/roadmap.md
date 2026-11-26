# 开发路线

## 概述 {#summary}

此文档作为项目版本路线与长期规划的总览索引，用于串联已发布/进行中的版本迭代记录，以及跨版本的主题规划方向。

备注：
- 此文档相比想法记录笔记（[ideas.md](ideas.md)）更加规范，相比面向用户的更新日志（[CHANGELOG.md](../../CHANGELOG.md)）拥有更多面向开发者的工程细节、重构项与分析决策链接。
- 具体版本的详细任务拆解、实施进度与交付检查点，统一维护在 [`roadmap/`](roadmap/) 下的单版本文件中，不在本文档重复维护正文与任务列表。
- 尚未纳入特定版本的跨版本专题规划、重构草案与探索蓝图，统一维护在 [`proposals/`](proposals/) 中（参见 [proposals.md](proposals.md)），实施落地后沉淀为 [`adr/`](adr/)。
- 版本迭代按主题和演进时机组织，不硬性预先锁定远期版本的具体功能清单。

---

## 按版本 {#by-version}

| 版本 | 状态 | 周期 | 概述 | 详细路线 |
| :--- | :--- | :--- | :--- | :--- |
| **`v3.1.0`** | 进行中 | `2026-11-19 ~` | 清场换代：GPL-3.0 许可证迁移、IDEA 2026.3 (`263`) 与 Kotlin 2.4 基线升级、维护者文档架构重整、`.agents/` 与首批 Skill 落地 | [roadmap/v3.1.0.md](roadmap/v3.1.0.md) |
| **`v3.0.7`** | 已完成 | `2026-11-24` | 紧急修复：兼容 Stellaris 4.6+ `common/name_lists` 移除 UTF-8 BOM 后的编码检查规范 | [roadmap/v3.0.7.md](roadmap/v3.0.7.md) |
| **`v3.0.6`** | 已完成 | `2026-11-04 ~ 2026-11-19` | 规则匹配与本地化命令修复、`Options` 体系、图片缓存/动作/图表异步化隔离、发布工作流解耦 | [roadmap/v3.0.6.md](roadmap/v3.0.6.md) |
| **`v3.0.5`** | 已完成 | `2026-10-13 ~ 2026-11-03` | 维护者笔记结构整理、轻量推流重构、命令行与图片处理隔离优化、JMH 微基准测试 | [roadmap/v3.0.5.md](roadmap/v3.0.5.md) |
| **`v3.0.4`** | 已完成 | `2026-09-30 ~ 2026-10-07` | 片段匹配初步支持、`WildcardAny/Literal`、Jackson 迁至 kotlinx-serialization、覆盖规则重命名 | [roadmap/v3.0.4.md](roadmap/v3.0.4.md) |
| **`v3.0.3`** | 已完成 | `2026-09-15 ~ 2026-09-23` | 规则匹配回归修复、语法/语义宏区分、并集/别名索引展开优化、模式与模板重构 | [roadmap/v3.0.3.md](roadmap/v3.0.3.md) |
| **`v3.0.2`** | 已完成 | `2026-08-10 ~ 2026-09-08` | 词法与语法支持完善、并集作用域、引号处理、`core` 模块测试用例与依赖整理 | [roadmap/v3.0.2.md](roadmap/v3.0.2.md) |
| **`v3.0.1`** | 已完成 | `2026-07-20 ~ 2026-08-05` | 测试作用域（`ChronicleTestScope`）、规则展开、大规模索引性能与内存剖析优化 | [roadmap/v3.0.1.md](roadmap/v3.0.1.md) |
| **`v3.0.0`** | 已完成 | `2026-07-09 ~ 2026-07-13` | 大重命名（Paradox Chronicle）与门面文档、参考手册体系重建 | [roadmap/v3.0.0.md](roadmap/v3.0.0.md) |
| **`v2.2.0`** | 已完成 | `~ 2026-07-05` | CSV 文件与 `rowConfig` 完整支持、并集规则初步支持 | [roadmap/v2.2.0.md](roadmap/v2.2.0.md) |
| **`v2.1.10`** | 已完成 | `~ 2026-06-24` | 解析器与代码补全重构、定值引用与标签集表达式支持、安全赋值运算符兼容 | [roadmap/v2.1.10.md](roadmap/v2.1.10.md) |

---

## 按主题 {#by-topic}

跨版本或尚未排期的主题规划已拆分为独立提案维护于 [`proposals/`](proposals/)（全量目录参见 [proposals.md](proposals.md)）：

| 主题 | 定位与核心方向 | 提案文档 |
| :--- | :--- | :--- |
| **合并索引优化** | 引入 `ParadoxMergedIndexThreadContext` 消除构建期冗余缓存访问与回溯计算 | [proposals/merged-index-thread-context.md](proposals/merged-index-thread-context.md) |
| **前缀标签匹配** | 支持匹配带前置标签的脚本表达式（如 `k = list v` 中的 `list`，`## prefix_tag`） | [proposals/prefix-tag-match.md](proposals/prefix-tag-match.md) |
| **内联定义** | 支持通过 `inline_script` 声明由传入参数决定的动态定义 | [proposals/inline-definition.md](proposals/inline-definition.md) |
| **次要语法与语言设施** | 脚本颜色词元切分、`[?PARAM]` 通配参数、高亮抑制选项与检查拆分 | [proposals/minor-syntax-enhancement.md](proposals/minor-syntax-enhancement.md) |
| **Data API** | 引入结构化、可序列化的 `ParadoxData` / `ParadoxDataSource` | [proposals/data-api.md](proposals/data-api.md) |
| **核心与外围 API 重构** | `select` / `search` / `presentation` / `extensions.diagram` / `visualization` 规范化 | [proposals/api-refactors.md](proposals/api-refactors.md) |
| **规则关联 QoL 特性** | 扩展选项 `## doc`、`## inject_doc` 及规则文件/路径引用解析 | [proposals/config-related-qol-features.md](proposals/config-related-qol-features.md) |
| **复杂表达式重构** | `icu.windea.pls.expression` 迁移、动态链接字面量与 Jomini 数据函数语义支持 | [proposals/complex-expression-refactors.md](proposals/complex-expression-refactors.md) |
| **宏展开与求值** | `script_value`、`inline_script`、`scripted_trigger/effect` 与定义注入展开预览 | [proposals/macro-explanation.md](proposals/macro-explanation.md) |
