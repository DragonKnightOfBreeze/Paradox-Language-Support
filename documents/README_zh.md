# 维护者文档

[By the dragon knight a coffee ☕](https://afdian.com/a/dk_breeze)

[English](README.md) | [中文](README_zh.md)

---

## 概述

此目录存放 **Paradox Chronicle** 面向维护者的公开文档。

不同于项目根目录下的通用文档以及 `docs/` 目录下面向终端用户的参考文档站点，本目录下的文档面向**项目维护者与 AI 协同代理**。
记录系统设计规范、开发规范、版本路线图、架构提案、架构决策档案、疑难排查案卷、评估分析报告等。

### 生命周期与认识论原则

本目录以**文档生命周期与权威度**（而非“人类手写 vs AI 生成”的作者身份）作为唯一组织坐标：

- **现行活规范（Living Specifications）**：随代码与工程实践持续演进的权威规范与看板（`design/`、`notes/guidelines*`、`notes/roadmap*`、`notes/proposals*`、`notes/ideas.md`、`notes/cheatsheet.md`）。
- **不可变历史卷宗（Immutable Archives）**：一经实施或结案即封存的历史切片（`notes/adr/`、`notes/issues/`、`notes/solutions/`、`notes/analysis/`、`notes/evaluations/`、`notes/reports/`）。历史案卷不追随后续微观代码重命名而回写；架构前提被推翻时，另立新 ADR 并标注 `Supersedes`。
- **事实层级校准**：文档仅记录意图、规范约定与历史切片，并非“唯一事实来源”。经过编译器与测试套件检验的项目源码更为准确可信，同时仍受制于 IntelliJ Platform 运行时机制、Paradox 游戏引擎实际行为以及当前尚未修复的缺陷。

### 语言与命名策略

- **正文语言**：主要以**简体中文**编写，以保证单人核心维护时的思维密度与表达精准度。非中文读者可借助翻译工具或 AI 助手查阅；外部贡献者补充维护者笔记时亦欢迎直接使用英文。
- **文件系统与路径**：全量推行**小写 kebab-case 英文命名**（如 `snippet-match.md`、`roadmap.md`），避免跨平台文件系统、Git 引用与 GitHub Web 端锚点兼容问题。

---

## 目录结构

```text
documents/
├── design/                 # 系统设计规范（UI/视觉体系、语言抽象模型、核心子系统等活规范）
├── notes/                  # 统一的维护者知识库（按“活规范 vs 不可变历史卷宗”组织）
│   ├── guidelines.md       # 现行代码架构、模块依赖、命名与格式规范
│   ├── guidelines/         # 测试指南与 JMH 微基准测试专册
│   ├── roadmap.md          # 按版本号总览索引与跨版本主题路由
│   ├── roadmap/            # 单版本开发路线与任务进度文件
│   ├── proposals.md        # 探索沙盒与未定稿架构蓝图索引
│   ├── proposals/          # 待评估/进行中的架构提案与主题草案
│   ├── ideas.md            # 想法记录与长期灵感池
│   ├── cheatsheet.md       # 实用技巧、实时模板与常用正则速查手册
│   ├── adr.md              # 已决并实施的架构决策档案索引
│   ├── adr/                # 封存的 RFC 风格架构决策档案
│   ├── issues.md           # 问题追踪与疑难诊断案卷索引
│   ├── issues/             # P1~Pnn 与 GitHub Issue 现场诊断案卷
│   ├── solutions.md        # 问题解决与避坑指南索引
│   ├── solutions/          # 环境配置、构建、依赖与平台陷阱解决方案
│   ├── analysis.md         # 性能剖析、现场快照与底层机制专题索引
│   ├── analysis/           # 长期性能/内存剖析轮次、线程转储分析与领域机制调研
│   ├── evaluations.md      # 阶段性架构与复杂度评估报告索引
│   ├── evaluations/        # 复杂度、内存设计与索引优化收益等专题评估快照
│   ├── reports.md          # 整理后的调研与现状报告索引
│   ├── reports/            # 代码风格收敛、测试覆盖度等文字报告卷宗
│   └── assets/             # 堆栈跟踪、线程转储与日志附件
├── changelogs/             # 历史更新日志归档（0.x / 1.x）
└── reports/                # 自动化工具定期生成的原始统计与跑分产物
```

### 1. `design/` (系统设计规范)

插件顶层设计、UI 视觉体系与核心抽象模型的现行设计规范：

- [PSI 接口架构规范](design/psi-architecture.md) - PSI 分层模型、接口继承谱系、跨语言能力接口、存根与轻量元素
- [语言抽象设计规范](design/language-abstractions.md) - Script、Localisation、CSV 与 CWT 的文法层与语义层抽象映射
- [复杂表达式体系设计规范](design/complex-expressions.md) - CWT 驱动的轻量表达式树、家族分类、节点模型与消费边界
- [作用域系统与上下文推导设计规范](design/scope-system.md) - 作用域分类、并集作用域模拟、CWT 作用域配置与上下文推断
- [脚本片段注入机制设计规范](design/script-injection.md) - 参数、本地化与 Markdown 注入宿主，动态规则上下文与嵌套边界
- [图标设计规范](design/icon-design.md) - 文件类型、AST 语法节点、规则元素、装订线、动作与通用图标的定义与颜色映射
- [配色方案规范](design/color-scheme.md) - 词法、语义与派生高亮键，主题 XML 重载与平台默认继承
- [提示词模板引擎规范](design/prompt-template-engine.md) - 内置 AI 提示词模板语法、指令与生命周期
- [模组导入与导出设计](design/mod-import-export.md) - 模组依赖导入导出架构、启动器格式探测与能力门控
- [外部检查工具集成：Tiger](design/tool-integration-tiger.md) - 五种 `*-tiger` 校验工具：provider、设置、命令执行、结果与缓存
- [领域名词与术语表](design/terminology.md) - 核心领域术语英汉对照与多载消歧
- [拼写检查机制设计](design/spellchecking.md) - 按语言划分的词元拆分与文法级拼写检查行为

### 2. `notes/` (维护者知识库)

#### 现行活规范

- [开发规范](notes/guidelines.md) - 插件的代码架构约定、模块依赖约束、命名与编码规范
  - [测试指南](notes/guidelines/testing.md) - 单元测试、平台集成测试（`ChronicleTestScope`）、测试 DSL、快照与受限测试
  - [基准测试指南](notes/guidelines/benchmarking.md) - JMH 微基准约定及其与真实环境 IDEA Profiling 的适用边界
- [开发路线](notes/roadmap.md) - 单版本索引（`notes/roadmap/`）与主题规划路由
- [架构提案](notes/proposals.md) - 探索沙盒与未定稿设计蓝图（`notes/proposals/`）
- [想法记录](notes/ideas.md) - 随时记录的灵感池与长期待办事项
- [实用技巧](notes/cheatsheet.md) - 实时模板、常用正则与脚本速查手册

#### 不可变历史卷宗

- [架构决策档案](notes/adr.md) - 已决并封存的架构决策索引（`notes/adr/`）
- [问题追踪与疑难分析](notes/issues.md) - 活跃问题与 P1~Pnn / Issue 诊断案卷（`notes/issues/`）
- [问题解决与避坑手册](notes/solutions.md) - 环境、构建、依赖与实现陷阱指南（`notes/solutions/`）
- [机制分析与性能剖析](notes/analysis.md) - 长期剖析轮次、故障现场转储与底层机制调研（`notes/analysis/`）
- [架构评估](notes/evaluations.md) - 阶段性复杂度、内存设计与索引收益评估快照（`notes/evaluations/`）
- [专题调研与现状报告](notes/reports.md) - 整理后的文字报告卷宗（`notes/reports/`，区别于 `reports/` 自动化产物）
