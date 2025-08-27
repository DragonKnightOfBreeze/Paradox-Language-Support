# 探索_14_pls-ai.xml

> 文件：`src/main/resources/META-INF/pls-ai.xml`
> 主题：PLS AI 能力（本地化文本的 AI 翻译/润色）相关的设置、意图与动作注册

---

## 总览

- 提供 AI 设置页，并围绕“本地化（PARADOX_LOCALISATION）”注册多条意图（Intention）与对应的动作（Action）。
- 主要能力聚焦：
  - 翻译（Translation）：从当前文本或指定 Locale 源翻译到目标文本。
  - 润色（Polishing）：对当前文本进行语言润色。

---

## 平台扩展（com.intellij）

- `applicationConfigurable`
  - `id="pls.ai"`，`parentId="pls"`
  - `bundle="messages.PlsBundle"`，`key="settings.ai"`
  - `instance="icu.windea.pls.ai.settings.PlsAiSettingsConfigurable"`
  - 作用：提供 AI 的配置入口（例如选择提供者/密钥/策略等，具体项见实现类）。

- `intentionAction`（均作用于 `PARADOX_LOCALISATION` 语言）
  - `AiCopyLocalisationWithTranslationIntention`
  - `AiReplaceLocalisationWithTranslationIntention`
  - `AiCopyLocalisationWithTranslationFromLocaleIntention`
  - `AiReplaceLocalisationWithTranslationFromLocaleIntention`
  - `AiCopyLocalisationWithPolishingIntention`
  - `AiReplaceLocalisationWithPolishingIntention`
  - 说明：覆盖“复制/替换 × 翻译/来自指定 Locale 翻译/润色”的 2×3 组合。

---

## 动作与菜单编组

- `actions`
  - `Pls.Manipulation.AiReplaceLocalisationWithTranslation`
  - `Pls.Manipulation.AiReplaceLocalisationWithTranslationFromLocale`
  - `Pls.Manipulation.AiReplaceLocalisationWithPolishing`
  - 均加入到 `Pls.LocalisationManipulation` 组，便于在本地化文件的操作集中使用。

---

## 观察与推断

- AI 功能主要面向“本地化文本”的智能生成/修订，与 `LocalisationManipulation` 工作流深度结合。
- 通过 Settings（`pls.ai`）可推测支持多提供者/参数化配置；与 `pls-integrations.xml`/`pls-extension-translation.xml` 的生态（外部翻译插件）具备潜在协作空间。
- Intention 与 Action 提供“点到点”式交互，适合小粒度文本处理；未来可在此基础上扩展为批量化操作。

---

## 展望：可实现的功能方向（建议）

- 交互与工作流
  - 批量处理：对选中文件/目录/Scope 批量“翻译/润色（复制或替换）”，带进度与可取消支持。
  - 预览与差异：在执行“替换”前展示对比视图（before/after），支持逐条确认与撤销。
  - Alt+Enter 增强：意图弹出中提供“预览变化”“仅本行/本段/整文件”粒度选项。
  - 历史与回溯：为每次 AI 操作记录“来源、参数、结果”，支持回滚与再次应用。

- 提供者与配置
  - 多提供者路由：在 `pls.ai` 设置页选择/优先级排序不同 AI/翻译提供者（如本地/云端）。
  - 上下文注入：向提示中自动注入 Mod/游戏上下文（如键名、作用域、变量），提升术语一致性。
  - 术语表/风格指南：与 Glossary 进行约束（强制用词、大小写、标点风格）。
  - 成本与速率：限制并发/速率；对话缓存/结果缓存以降低调用开销。

- 与现有子系统联动
  - 与 `pls-integrations.xml`：可挂接翻译工具提供者或外部 CLI 工具，实现“离线/本地”能力。
  - 与 `pls-extension-translation.xml`：若装有第三方翻译插件，可作为“提供者”融入统一入口。
  - 与检查/Lint：在替换后触发本地化检查，自动修复常见占位符/格式问题。

- 质量与安全
  - 保护变量与占位符：在翻译/润色时锁定 `{}`/`$...`/`%...%` 等占位符不被改写。
  - 本地化规则感知：保留换行/转义/富文本标记（如 §/颜色标签），确保在游戏中可用。
  - 密钥管理：引导用户以安全方式配置 API Key（IDE 安全存储），避免明文泄露。

- 开发与测试
  - 单元/集成测试：对各意图与动作的行为进行测试（成功/失败/撤销路径）。
  - 可观测性：为 AI 调用增加日志与诊断信息（含请求摘要、耗时与错误处理）。

---

## 关联与后续

- 直接关联：
  - `src/main/resources/META-INF/pls-integrations.xml`（可选外部工具/提供者）
  - `src/main/resources/META-INF/pls-extension-translation.xml`（第三方翻译插件集成）
  - `src/main/resources/META-INF/pls-intentions.xml`（与一般意图系统的分工）
- 建议下一步：阅读 `pls-inject.xml` 或补充 `pls-extension-translation.xml`，以串联注入/翻译生态。
