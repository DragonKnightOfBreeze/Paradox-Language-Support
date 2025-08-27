# 探索_18_pls-extension-translation.xml

> 文件：`src/main/resources/META-INF/pls-extension-translation.xml`
> 主题：与 Translation Plugin 的联动与文档翻译覆盖

---

## 总览

- 当安装 `cn.yiiguxing.plugin.translate` 插件时，PLS 通过可选依赖的 `config-file` 挂载本扩展：
  - 为 CWT/Script/Localisation 三种语言在“快速文档”上优先（`order="first"`）接入 `TranslatedDocumentationProvider`。
  - 在 PLS 自有的集成扩展点上注册 `translationToolProvider`，由 `PlsTranslationPluginToolProvider` 提供适配。

---

## 扩展（com.intellij）

- `lang.documentationProvider`（3 条）：
  - `language="CWT" | "PARADOX_SCRIPT" | "PARADOX_LOCALISATION"`
  - `order="first"`，实现：`cn.yiiguxing.plugin.translate.provider.TranslatedDocumentationProvider`
  - 说明：在显示 PSI 元素 QuickDoc 时，优先由翻译插件提供内容（或叠加译文）。

---

## 扩展（icu.windea.pls.integrations）

- `translationToolProvider` → `PlsTranslationPluginToolProvider`
  - 作用：将 Translation Plugin 暴露为 PLS 的“翻译工具提供者”，统一纳入 `pls-integrations.xml` 所定义的工具生态。

---

## 观察与建议

- 用户体验：文档翻译优先级较高，建议在设置中提供“译文优先/并排/悬浮层”模式；大文档可启用懒加载以控时。
- 可靠性：翻译服务可能依赖网络/令牌，需清晰的失败回退与缓存策略（避免阻塞 QuickDoc）。
- 生态闭环：与 `pls-ai.xml` 的替换/润色意图、`pls-integrations.xml` 的工具提供者机制互补，可形成“解释/翻译/改写”的一体工作流。

---

## 下一步

- 验证覆盖：在三种语言的典型元素上触发 QuickDoc，确认翻译优先级与回退行为；检查 `PlsTranslationPluginToolProvider` 的能力暴露是否可被其他功能复用。
