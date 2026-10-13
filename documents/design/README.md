# 系统设计规范 (Design Specifications)

## 概述 / Overview

本目录存放 Paradox Chronicle 插件的各项系统设计规范与技术规格说明（Design Specs）。

不同于 `docs/` 目录下提供给外部终端用户的参考文档，也不同于 `notes/adr/` 下记录历史决定的架构决策档案，这里的文档是面向系统架构与模块实现的**“活设计规范（Living Specifications）”**。

本目录下的文档采取**人机共建、协同维护**的原则：维护者确立设计原则与标准，AI 协助整理、扩充与同步更新。

---

This directory contains system design specifications and technical specs for Paradox Chronicle.

Unlike the reference documentation in `docs/` (for end users) and architectural decision records in `notes/adr/` (for historical decisions), the documents here are **living specifications** for system architecture and module implementations.

Documents here are maintained collaboratively by human maintainers and AI assistants.

## 规范清单 / Specifications Index

- **UI & 视觉设计**
  - [图标设计规范 (Icon Design)](icon-design.md) - 文件类型、AST 节点、规则元素图标定义与映射
  - [配色方案规范 (Color Scheme)](color-scheme.md) - CWT、Script、Localisation、CSV 各语言的词法与语义高亮配色
- **领域与语言抽象**
  - [领域名词与术语表 (Terminology)](terminology.md) - 核心术语英汉对照与标准命名
  - [语言抽象设计规范 (Language Abstractions)](language-abstractions.md) - 文法层与语义层的抽象模型映射
- **子系统与功能设计**
  - [提示词模板引擎规范 (Prompt Template Engine)](prompt-template-engine.md) - 内置 AI 模板引擎语法、指令与生命周期
  - [模组导入与导出设计 (Mod Import/Export)](mod-import-export.md) - 模组依赖导入导出架构与格式适配
  - [拼写检查机制设计 (Spellchecking)](spellchecking.md) - 词元拆分与文法级拼写检查
  - [脚本片段注入机制设计 (Script Injection)](script-injection.md) - 跨语言注入与动态规则上下文构建
  - [外部检查工具集成：Tiger (Tool Integration: Tiger)](tool-integration-tiger.md) - ck3-tiger 校验工具集成架构
