# 报告_#7_项目探索_task-3_会话六

> 会话日期：2025-08-27 17:46（本地）  
> 会话主题：一次性阅读三个 extension 配置并同步报告（探索_16/17/18）

---

## 概述

- 本次目标：
  - 阅读 `src/main/resources/META-INF/pls-extension-markdown.xml`、`pls-extension-diagram.xml`、`pls-extension-translation.xml`。
  - 为每个文件生成探索笔记，更新汇总报告“阶段更新/待办与下一步/变更历史”。
- 已完成：
  - 新建探索笔记：
    - `notes/ai-vision/task-3/探索_16_pls-extension-markdown.xml.md`
    - `notes/ai-vision/task-3/探索_17_pls-extension-diagram.xml.md`
    - `notes/ai-vision/task-3/探索_18_pls-extension-translation.xml.md`
  - 更新汇总报告：`notes/ai-reports/报告_#2_项目探索_task-3.md` 新增探索_16/17/18，调整“待办与下一步”为“include 阅读已完成，转入源码实现与验证”，并追加三条变更历史。

---

## 阶段更新：阅读 pls-extension-markdown.xml（探索_16）

- 隐式引用：`MarkdownInlineCodeReferenceProvider`（内联代码）。
- 检查：`PlsMarkdownUnresolvedReferenceLink`（WARNING，默认启用）。
- 高级设置：`pls.md.resolveInlineCodes`（默认 false）。
- 注入后处理：`MarkdownCodeFenceInjectedFileProcessor`。

---

## 阶段更新：阅读 pls-extension-diagram.xml（探索_17）

- 设置页：`PlsDiagramSettingsConfigurable`（`id=pls.diagram`，父 `pls`）。
- Diagram Provider：Stellaris/Ck2/Ck3/Eu4/Hoi4/Ir/Vic2/Vic3 的事件树或科技树。
- Actions：将“打开设置”加入 Diagram 工具栏；把 `Pls.GotoGroup` 注入 UML 菜单与节点编辑菜单；将 `UML.Group` 引入 PLS 层级菜单。

---

## 阶段更新：阅读 pls-extension-translation.xml（探索_18）

- QuickDoc 翻译优先：为 `CWT/Script/Localisation` 注册 `TranslatedDocumentationProvider`（order="first"）。
- 集成提供者：`PlsTranslationPluginToolProvider`（归入 `icu.windea.pls.integrations.translationToolProvider` 生态）。

---

## 待办与下一步

- include 配置阅读已完成（Markdown/Diagram/Translation）。
- 建议转入源码实现阅读与功能验证：
  - Markdown：隐式引用与 Inspection 的触发条件与耗时观测。
  - Diagram：Provider 渲染/过滤/导航链路与性能基线。
  - Translation：QuickDoc 译文优先/回退策略、缓存与网络失败处理。

---

## 会话总结

- 完成：三个 extension 配置的系统化阅读，创建探索笔记，汇总报告追加三段阶段更新并清空 include 待读。
- 状态：无阻塞；下一阶段进入源码与验证。
