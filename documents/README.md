# 维护者文档体系 (Maintainer Documents)

[English](#overview) | [中文](#概述)

---

## 概述

此目录存放 **Paradox Chronicle** 插件的全部维护者内部文档（Maintainer Documents）。

不同于项目根目录下的通用文档（如 `README.md`）以及 `docs/` 目录下面向最终用户的参考文档站点，本目录下的文档是面向**项目核心作者、长期维护者以及 AI 协同代理**的。其中记录了深入的系统架构规范、历史架构决策、疑难排查日志、性能指标剖析以及演进路线。

### 语言与命名策略

- **正文语言**：主要以**中文**编写。这是为了保证单人核心维护时的思维密度、表达精准度并消除不必要的母语转换摩擦。
- **国际化与翻译**：非中文母语的读者或开发者若出于兴趣需查阅本文档库，推荐借助现代翻译引擎或 AI 助手（如 DeepL, ChatGPT 等）辅助阅读；未来如有外部英文贡献者补充维护者笔记，亦完全支持并欢迎直接提交英文版本。
- **文件系统与路径**：全量推行**小写 kebab-case 英文命名**（如 `snippet-match.md`, `roadmap.md`）。这彻底解决了跨平台文件系统兼容、Git 引用、Markdown 内部链接跳转以及 URL 编码在 GitHub Web 端丢失锚点的问题。

---

## Overview

This directory stores all internal maintainer documentation for the **Paradox Chronicle** plugin.

Unlike public user documentation in `docs/`, the documents here are intended for **project core authors, long-term maintainers, and AI coding agents**. They record in-depth architectural specifications, historical Architecture Decision Records (ADRs), diagnostic post-mortems, profiling benchmarks, and development roadmaps.

### Language & Naming Policy

- **Content Language**: Primarily written in **Simplified Chinese** to maximize precision, depth, and maintenance efficiency for the core maintainer. Non-Chinese readers are warmly encouraged to use AI assistants or translation engines. English contributions to maintainer notes are welcome.
- **File System & Paths**: All file and directory names strictly adhere to **English kebab-case** (e.g., `snippet-match.md`, `roadmap.md`), completely avoiding URL-encoding glitches and anchor-breaking issues across GitHub, IDEs, and tools.

---

## 目录结构与权责边界 / Architecture & Boundaries

```text
documents/
├── design/                 # 【系统设计规范（Design Specs）】（人机协同演进的活规范）
├── notes/                  # 【人工维护的核心心智区】（权威、持续维护）
│   ├── adr/                # 架构决策档案（自包含 RFC 风格，永不过期）
│   ├── issues/             # 疑难深度排查档案（P1~Pnn）与待办清单
│   ├── solutions/          # 避坑手册与开发技巧解决方案
│   ├── analysis/           # 性能与内存长期基准测试轮次（活笔记）
│   ├── assets/             # 关联的堆栈跟踪、线程转储与日志附件
│   ├── roadmap.md          # 全局版本看板与路线图
│   ├── guidelines.md       # 架构约定、模块依赖与开发规范
│   ├── ideas.md            # 灵感池与 Backlog
│   └── cheatsheet.md       # 实时模板与常用正则速查手册
├── ai-notes/               # 【AI 协助生成与原样归档区】（免持续维护免责）
│   ├── analysis/           # 针对具体 Bug、死锁的现场排查快照
│   ├── reports/            # 复杂度评估、技术调研与覆盖率报告
│   ├── study/              # 第三方技术与概念学习备忘（Gradle, MCP, LSP 等）
│   └── adr/                # 起草中或备选的架构提案
├── changelogs/             # 历史版本更新日志归档
└── reports/                # 自动化脚本定期生成的代码/配置统计快照
```

### 1. `design/` (系统设计规范)
- **定位**：插件顶层设计、UI 视觉体系与核心抽象模型的“活设计规范（Living Specifications）”。
- **维护原则**：**人机共建**。维护者确立标准与审美，AI 协助整理、补全与保持代码同步。
- **核心文件**：
  - [图标设计规范 (icon-design.md)](design/icon-design.md)
  - [配色方案规范 (color-scheme.md)](design/color-scheme.md)
  - [提示词模板引擎规范 (prompt-template-engine.md)](design/prompt-template-engine.md)
  - [模组导入与导出设计 (mod-import-export.md)](design/mod-import-export.md)
  - [语言抽象设计规范 (language-abstractions.md)](design/language-abstractions.md)
  - [领域名词与术语表 (terminology.md)](design/terminology.md)

### 2. `notes/` (人工核心心智区)
- **定位**：由项目核心维护者亲自编写、审阅与维护的权威知识库。
- **子模块划分**：
  - **[notes/adr/ (架构决策档案)](notes/adr/README.md)**：每个技术决策独立成篇（如 `snippet-match.md`、`replace-jackson-with-kotlinx-serialization.md`）。采用**自包含 RFC 风格**，同时记录问题背景、决策方案、代码落点、用法范例与任务清单。一经实施即封存，永不过期。
  - **[notes/issues/ (问题追踪与排查)](notes/issues/README.md)**：保留顶层待办清单，将 P1~Pnn 等深难疑难排查独立成篇（如 `p18-startup-freeze-profiling.md`）。
  - **[notes/solutions/ (避坑与解决方案)](notes/solutions/README.md)**：按主题沉淀日常开发配置、环境代理、第三方库排坑指南。
  - **[notes/analysis/ (性能与内存基准)](notes/analysis/README.md)**：长期跟进的大型游戏目录索引耗时与堆内存测试轮次记录。
  - **全局单文件**：
    - [roadmap.md (开发路线图)](notes/roadmap.md)
    - [guidelines.md (开发与代码规范)](notes/guidelines.md)
    - [ideas.md (想法记录与灵感池)](notes/ideas.md)
    - [cheatsheet.md (实用技巧速查)](notes/cheatsheet.md)

### 3. `ai-notes/` (AI 协同归档区)
- **定位**：低心理阻力的知识沉淀区与免责暂存区。
- **免责声明**：本目录下的笔记**按原样归档（Archived as-is）**，主要记录当时现场的调查数据、学习备忘或评估草稿，**不承担与后续代码保持实时同步的维护责任**。成熟设计可按需晋升至 `design/` 或 `notes/adr/`。

---

## 增量编写规范建议 / Writing Guidelines for Future Notes

为了让未来沉淀的笔记更加标准、周全，建议遵循以下简明模板结构：

### ADR 编写结构 (RFC-Style)
新建 `documents/notes/adr/{kebab-case-title}.md`：
```markdown
# 决策标题

> 所属模块：`大类 > 子类`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v3.x.x
> - 日期：YYYY-MM-DD
> - 状态：提议中 / 进行中 / 已实施 / 已废弃

## 背景与问题 (Context)
说明为什么当前架构不满足需求，遇到了什么异常、瓶颈或平台升级兼容性问题。

## 决策方案 (Decision)
核心设计思想、数据模型或选型结果。

## 关键代码位置 (Key Locations)
- [ ] `icu.windea.pls.xxx.XxxService`
- [ ] `icu.windea.pls.xxx.XxxEP`

## 用法说明 (Usage)
必要的代码调用范例、配置项或 DSL 语法。

## 实施任务清单 (Tasks)
- [x] 第一阶段：初步适配
- [ ] 第二阶段：完善回归测试
```

### 疑难排查编写结构 (Issue Diagnostic)
新建 `documents/notes/issues/p{nn}-{kebab-case-title}.md`：
```markdown
# P{nn} 现象简述

> 上级导航：[问题追踪与分析索引 (Issues Index)](README.md)

> - 状态：已解决 / 不再复现 / 追踪中
> - 关键异常/报错：`ClassNameException`

堆栈跟踪：[stack-trace-X.txt](../assets/stack-trace-X.txt)

## 现象与复现步骤
简述触发条件或代码片段。

## 根因推导与假设验证
记录调试过程、排查推理链或多线程时序分析。

## 解决方案与结论
修复手段及预防措施。
```
