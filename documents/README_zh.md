# 维护者文档

[By the dragon knight a coffee ☕](https://afdian.com/a/dk_breeze)

[English](README.md) | [中文](README_zh.md)

---

## 概述

此目录存放 **Paradox Chronicle** 面向维护者的公开文档（公开部分）。

不同于项目根目录下的通用文档以及 `docs/` 目录下面向终端用户的参考文档站点，本目录下的文档面向**项目维护者与 AI 协同代理**。记录深入的系统设计规范、架构决策档案、疑难排查日志、性能基准剖析以及版本路线图。

### 语言与命名策略

- **正文语言**：主要以**中文**编写。这是为了保证单人核心维护时的思维密度、表达精准度并消除母语转换摩擦。
- **国际化与翻译**：非中文母语的读者或开发者若出于兴趣需查阅本文档库，推荐借助现代翻译引擎或 AI 助手（如 DeepL, ChatGPT 等）辅助阅读；未来如有外部英文贡献者补充维护者笔记，亦完全支持并欢迎直接提交英文版本。
- **文件系统与路径**：全量推行**小写 kebab-case 英文命名**（如 `snippet-match.md`, `roadmap.md`）。这彻底解决了跨平台文件系统兼容、Git 引用以及 URL 编码在 GitHub Web 端丢失锚点的问题。

## 目录结构与分类准则

```text
documents/
├── design/                 # 系统设计规范（UI/视觉体系、语言抽象模型、核心子系统设计等活规范）
├── notes/                  # 维护者笔记（原则上必须由人类编写，权威、持续维护）
│   ├── adr.md              # 架构决策档案索引
│   ├── adr/                # 自包含架构决策档案（语法解析演进、依赖框架替换、规则扩展等）
│   ├── issues.md           # 问题追踪与疑难分析索引
│   ├── issues/             # 疑难故障与异常诊断记录（P1~Pnn，如索引冲突、并发死锁、SOF等）
│   ├── solutions.md        # 解决方案与避坑指南索引
│   ├── solutions/          # 具体环境配置、依赖冲突与实现陷阱处理方案
│   ├── analysis.md         # 性能与内存长期基准分析索引
│   ├── analysis/           # 索引吞吐耗时与堆内存占用的长期测试轮次记录
│   ├── assets/             # 堆栈跟踪、线程转储与日志附件
│   ├── roadmap.md          # 版本开发路线与待办看板
│   ├── guidelines.md       # 代码规范、模块依赖约束与开发约定
│   ├── ideas.md            # 想法记录与长期灵感池
│   └── cheatsheet.md       # 实用技巧、实时模板与常用正则速查手册
├── ai-notes/               # AI 协同笔记归档（原样归档，免持续维护责任）
│   ├── analysis.md         # 现场排查与机制分析索引
│   ├── analysis/           # 故障排查快照与领域底层机制专题分析
│   ├── solutions.md        # 环境配置与构建避坑手册索引
│   ├── solutions/          # 具体环境与构建问题解决方案
│   ├── reports.md          # 评估与现状报告索引
│   ├── reports/            # 项目复杂度、代码覆盖率、潜在竞品机制对比与技术趋势评估
│   ├── study.md            # 通用技术与概念学习备忘索引
│   ├── study/              # 外部第三方技术、平台 SDK 与 AI 框架学习笔记
│   └── adr.md              # 架构提案索引（备选或起草中的架构预研）
├── changelogs/             # 历史更新日志归档
└── reports/                # 自动化工具定期生成的代码与配置统计报告
```

### 1. design/ (系统设计规范)

插件顶层设计、UI 视觉体系与核心抽象模型的活设计规范（Living Specifications），采取人机共建原则，由维护者确立原则，AI 协助整理与保持同步：

- [图标设计规范](design/icon-design.md) - 文件类型、AST 语法节点与规则元素的图标定义与颜色映射
- [配色方案规范](design/color-scheme.md) - 各支持语言的词法高亮与语义高亮配色规范
- [提示词模板引擎规范](design/prompt-template-engine.md) - 内置 AI 模板引擎语法、指令与生命周期
- [模组导入与导出设计](design/mod-import-export.md) - 模组依赖导入导出架构与格式适配
- [语言抽象设计规范](design/language-abstractions.md) - 文法层与语义层的抽象模型映射
- [领域名词与术语表](design/terminology.md) - 核心术语英汉对照与标准命名
- [拼写检查机制设计](design/spellchecking.md) - 词元拆分与文法级拼写检查
- [脚本片段注入机制设计](design/script-injection.md) - 跨语言注入与动态规则上下文构建
- [外部检查工具集成：Tiger](design/tool-integration-tiger.md) - tiger 校验工具集成架构

### 2. notes/ (维护者笔记)

由项目核心维护者编写、审阅与维护的权威知识库。**原则上必须由人类编写（除极少数格式整理等特殊情况）**，以保证其思考的真实性与权威性。架构决策（ADR）采用自包含 RFC 风格，一经实施即封存，永不过期。

核心看板与伴生索引入口：
- [开发规范](notes/guidelines.md) - 插件的代码架构约定、模块依赖约束与编码规范
- [开发路线](notes/roadmap.md) - 按版本追踪的任务计划、重构待办与完成项
- [想法记录](notes/ideas.md) - 随时记录的灵感池与长期待办事项
- [实用技巧](notes/cheatsheet.md) - 实时模板、常用正则与脚本速查手册
- [架构决策索引](notes/adr.md) - 自包含架构决策档案的分类目录与索引
- [问题追踪索引](notes/issues.md) - 活跃调试与 GitHub 待办清单，以及 P1~Pnn 疑难深度分析索引
- [问题解决索引](notes/solutions.md) - 日常开发配置、环境代理与第三方库避坑指南
- [性能与内存分析索引](notes/analysis.md) - 长期维护的游戏目录索引耗时与堆内存基准测试轮次

### 3. ai-notes/ (AI 协同归档区)

低心理阻力的知识沉淀区与免责暂存区。文档**按原样归档（Archived as-is）**，**不承担与后续代码保持实时同步的维护责任**。成熟机制可按需晋升至 `design/` 或 `notes/adr/`。

伴生索引入口：
- [现场排查与机制分析索引](ai-notes/analysis.md) - 现场故障排查与底层机制深度分析
- [问题解决与避坑指南索引](ai-notes/solutions.md) - 环境配置与构建避坑手册
- [评估与现状报告索引](ai-notes/reports.md) - 自身演进评估、潜在竞品机制对比与技术调研
- [技术与概念学习笔记索引](ai-notes/study.md) - 外部第三方技术、平台 SDK 与 AI 框架学习备忘
- [架构提案索引](ai-notes/adr.md) - AI 协同起草的技术预研与架构备选方案
