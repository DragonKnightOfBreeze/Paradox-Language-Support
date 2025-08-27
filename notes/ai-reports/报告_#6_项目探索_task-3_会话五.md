# 报告_#6_项目探索_task-3_会话五

> 会话日期：2025-08-27（本地）  
> 会话主题：阅读 `pls-integrations.xml`（探索_13）、`pls-ai.xml`（探索_14）与 `pls-inject.xml`（探索_15）并同步报告

---

## 概述

- 本次目标：
  - 阅读并整理 `src/main/resources/META-INF/pls-integrations.xml` 与 `src/main/resources/META-INF/pls-ai.xml`。
  - 产出探索笔记并同步至汇总/会话报告。
- 已完成：
  - 新建探索笔记：`notes/ai-vision/task-3/探索_13_pls-integrations.xml.md`。
  - 更新汇总报告：`notes/ai-reports/报告_#2_项目探索_task-3.md` 新增阶段更新与变更历史（探索_13），调整“待办与下一步”。
  - 更新会话报告：`notes/ai-reports/报告_#5_项目探索_task-3_会话四.md` 追加“探索_13”阶段更新与调整待办。
  - 新建探索笔记：`notes/ai-vision/task-3/探索_14_pls-ai.xml.md`（含“展望：可实现的功能方向”）。
  - 更新汇总报告：`notes/ai-reports/报告_#2_项目探索_task-3.md` 新增“探索_14”阶段更新、调整待办并追加变更历史。
  - 新建探索笔记：`notes/ai-vision/task-3/探索_15_pls-inject.xml.md`。
  - 更新汇总报告与会话报告：新增“探索_15”阶段更新，待办仅保留 `pls-extension-translation.xml`。

---

## 阶段更新：阅读 pls-integrations.xml（探索_13）

- 设置页：`applicationConfigurable` -> `PlsIntegrationsSettingsConfigurable`（`id=pls.integrations`，`key=settings.integrations`）。
- VFS 监听：`PlsTigerConfFileListener`（异步监听 Lint 配置变更）。
- 扩展点（`dynamic=true`）：
  - `icu.windea.pls.integrations.imageToolProvider`（`PlsImageToolProvider`）
  - `icu.windea.pls.integrations.translationToolProvider`（`PlsTranslationToolProvider`）
  - `icu.windea.pls.integrations.lintToolProvider`（`PlsLintToolProvider`）
- 实现（`defaultExtensionNs="icu.windea.pls.integrations"`）：
  - 图像工具：`PlsTexconvToolProvider`、`PlsMagickToolProvider`
  - Lint 工具：`PlsTigerLintToolProvider$Ck3`、`$Ir`、`$Vic3`
  - 翻译工具：注释示例 `PlsTranslationPluginToolProvider`（见 `pls-extension-translation.xml`）

---

## 阶段更新：阅读 pls-ai.xml（探索_14）

- 设置页：`applicationConfigurable` -> `PlsAiSettingsConfigurable`（`id=pls.ai`，父分组 `pls`，`key=settings.ai`）。
- 意图（语言：`PARADOX_LOCALISATION`）：
  - 复制/替换 × 翻译/来自 Locale 翻译/润色 共 6 种 `Ai*Intention`。
- 动作：
  - `Pls.Manipulation.AiReplaceLocalisationWithTranslation`
  - `Pls.Manipulation.AiReplaceLocalisationWithTranslationFromLocale`
  - `Pls.Manipulation.AiReplaceLocalisationWithPolishing`
  - 均挂到 `Pls.LocalisationManipulation` 组。
- 展望（节选，详见探索笔记）：
  - 批量处理与差异预览、可撤销；
  - 多提供者路由与术语/风格约束；
  - 占位符保护与本地化规则感知；
  - 调用日志、速率/成本控制与缓存。

---

## 阶段更新：阅读 pls-inject.xml（探索_15）

- 核心要点：
  - 扩展点：`codeInjectorSupport`、`codeInjector`、`injectedFileProcessor`（`dynamic=true`）。
  - 应用监听：`CodeInjectorService$Listener` 订阅 `AppLifecycleListener`（随应用生命周期装配/清理注入）。
  - Support：`BaseCodeInjectorSupport`、`FieldBasedCacheCodeInjectorSupport`。
  - 注入器：核心能力修正、图像读取优化（DDS/TGA）、附加能力、PSI 性能优化。
  - 备注：注释的 `ImageDescriptorKtCodeInjector` 作为 bugfix 预备位。
- 笔记文件：`notes/ai-vision/task-3/探索_15_pls-inject.xml.md`

---

## 待办与下一步

- 请选择下一个 include 文件（优先其一）：
  - `src/main/resources/META-INF/pls-extension-translation.xml`

---

## 会话总结

- 完成：阅读并记录 `pls-integrations.xml`、`pls-ai.xml`、`pls-inject.xml`；创建探索笔记并同步汇总/会话报告；本报告用于记录会话五进展。
- 状态：无阻塞；下一步阅读 `pls-extension-translation.xml` 并产出探索笔记。
