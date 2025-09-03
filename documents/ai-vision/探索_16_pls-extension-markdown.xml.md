# 探索_16_pls-extension-markdown.xml

> 文件：`src/main/resources/META-INF/pls-extension-markdown.xml`
> 主题：对 Markdown 的扩展能力与注入后处理

---

## 总览

- 面向 Markdown 的轻量扩展：内联代码引用解析、未解析引用链接检查、可选的内联代码解析开关。
- 注入后处理：为代码围栏（code fence）注入产生的文件/片段提供处理器，融入 PLS 的注入生态。

---

## 扩展（com.intellij）

- `psi.implicitReferenceProvider` → `MarkdownInlineCodeReferenceProvider`
  - 为 Markdown 内联代码提供隐式引用解析能力（如对 PLS 语言片段的识别与跳转）。
- `localInspection`（language="Markdown"）
  - `shortName=PlsMarkdownUnresolvedReferenceLink`、`level=WARNING`、默认启用。
  - 文案与分组来自 `messages.PlsMarkdownBundle`（`inspection.unresolvedReferenceLinks`、`inspection.group`）。
  - 实现：`MarkdownUnresolvedReferenceLinkInspection`。
- `advancedSetting`
  - `id=pls.md.resolveInlineCodes`、`default=false`、`groupKey=group.advanced.settings`、`bundle=messages.PlsMarkdownBundle`。
  - 提供“是否解析内联代码”的实验性或高阶开关。

---

## 注入后处理（icu.windea.pls.inject）

- `injectedFileProcessor` → `MarkdownCodeFenceInjectedFileProcessor`
  - 对 Markdown 代码围栏注入的内容进行后处理（语义修补、索引告知、缓存等）。

---

## 观察与建议

- 能力边界：`advancedSetting` 默认关闭，适合在大型文档/仓库中控噪；可考虑按文件大小/目录范围启用。
- 诊断建议：为引用解析与检查增加“问题计数/耗时”指标，便于在大项目中观测影响。
- 生态联动：结合 PLS 的语言解析器，可实现从 Markdown 文档到脚本/本地化的交叉跳转与预览。

---

## 关联

- 注入生态：`pls-inject.xml` 中的 `InjectedFileProcessor` 扩展点。
- 文案包：`messages.PlsMarkdownBundle`。

---

## 下一步

- 若需要继续：进入实现类阅读与快速验证（小型 Markdown 文档）以评估引用解析与检查的效果。
