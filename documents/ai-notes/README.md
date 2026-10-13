# AI 协同笔记归档 (AI Notes Archive)

## 概述 / Overview

本目录用于存放由 AI 协助生成、探索或整理的各类技术笔记与分析报告。

**重要说明**：
- 本目录下的文档**基本上是按原样归档的（Archived as-is）**。
- 项目的代码实现、架构决策或相关类名在文档归档后可能随版本演进而发生变化，本目录下的历史文档**不承担与最新代码实时保持同步的维护责任**。
- 维护者可以在此以极低心智阻力快速沉淀日常 AI 探索成果、堆栈诊断记录与竞品评估。当某些机制或方案经过沉淀、确认为长期正式标准时，将按需晋升并整理至 `documents/design/`（系统设计规范）或 `documents/notes/adr/`（架构决策档案）。

---

This directory stores various technical notes, explorations, and diagnostic reports generated or assisted by AI agents.

**Notice**:
- Documents in this directory are **archived as-is**.
- Code references, class names, or implementation ideas may change over time after archiving. These documents are **not required to be continuously maintained or synchronized** with the latest codebase.
- Mature concepts or design decisions from this directory may be promoted and formalized into `documents/design/` or `documents/notes/adr/`.

## 目录索引 / Catalog

- **[analysis/ (现场排查与专项分析)](analysis/)**：针对具体 Bug、死锁、线程卡死或偶发异常的现场勘查与根因推导快照。
  - `analysis-startup-freeze-thread-dump-1871846717.md` - 启动冻结与线程转储分析
  - `analysis-p19-localisation-find-usages-sof.md` - 本地化查找用法 SOF 分析
  - `analysis-cwt-override-rules.md` - 规则系统覆盖机制剖析
  - `analysis-opencode-windows-gradle-timeout.md` - Gradle 命令执行超时分析
  - `analysis-verify-plugin-signature-no-source.md` - 插件签名校验 NO-SOURCE 排查
- **[reports/ (评估与调研报告)](reports/)**：针对项目自身或外部技术的评估、权衡与现状报告（`eval-*.md` 为评估，`report-*.md` 为报告）。
  - `eval-project-complexity-a.md` / `eval-project-complexity-b.md` - 项目复杂度评估
  - `eval-index-optimization-*.md` - 索引性能与内存优化评估
  - `report-config-expression-conventions.md` - 规则表达式代码风格与设计约定报告
  - `report-test-coverage-core-3.0.1.md` - Core 模块测试覆盖度报告
- **[study/ (技术与概念学习笔记)](study/)**：针对 IntelliJ SDK、Gradle、Kotlin、AI 技术（RAG/MCP/Prompt Engineering）等的学习与理解备忘。
  - `study-intellij-platform-sdk.md` - IntelliJ Platform SDK 学习笔记
  - `study-gradle.md` - Gradle 构建机制学习笔记
  - `study-mcp.md` - Model Context Protocol (MCP) 学习笔记
  - `study-langchain4j.md` - LangChain4j 框架学习笔记
  - `study-cwt-rules.md` - CWT 规则基础概念
  - `study-vuepress-syntax-highlighters.md` - VuePress 语法高亮实现总结
  - 以及更多（Kotlin、Ktor、Ktorm、Caffeine、RAG 等）
- **[adr/ (起草中的决策提案)](adr/)**：AI 协助起草备选、尚未被正式采纳合并至 `notes/adr/` 的方案提案（备用）。
