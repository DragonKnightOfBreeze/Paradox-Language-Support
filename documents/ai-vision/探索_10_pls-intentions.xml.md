# 探索_10_pls-intentions.xml

> 时间：2025-08-27 16:52（本地）
> 来源文件：`src/main/resources/META-INF/pls-intentions.xml`
> 目的：梳理并分类本插件注册的 IntentionAction（按语言域：CWT/Script/Localisation/CSV），关注 `categoryKey`、描述资源与实现类命名。

---

## 一、注册概览（com.intellij:intentionAction）

- CWT（`language=CWT`，`categoryKey=intention.cwt.category`）
  - `icu.windea.pls.lang.intentions.cwt.QuoteIdentifierIntention`（`descriptionDirectoryName=CwtQuoteIdentifierIntention`）
  - `icu.windea.pls.lang.intentions.cwt.UnquoteIdentifierIntention`（`descriptionDirectoryName=CwtUnquoteIdentifierIntention`）

- Paradox Script（`language=PARADOX_SCRIPT`，`categoryKey=intention.script.category`）
  - `icu.windea.pls.lang.intentions.script.QuoteIdentifierIntention`
  - `icu.windea.pls.lang.intentions.script.UnquoteIdentifierIntention`
  - `icu.windea.pls.lang.intentions.script.CopyScriptedVariableNameIntention`
  - `icu.windea.pls.lang.intentions.script.CopyScriptedVariableLocalizedNameIntention`
  - `icu.windea.pls.lang.intentions.script.CopyDefinitionNameIntention`
  - `icu.windea.pls.lang.intentions.script.CopyDefinitionLocalizedNameIntention`
  - `icu.windea.pls.lang.intentions.script.CopyLocalisationNameIntention`
  - `icu.windea.pls.lang.intentions.script.CopyLocalisationTextIntention`
  - `icu.windea.pls.lang.intentions.script.CopyLocalisationTextAsPlainIntention`
  - `icu.windea.pls.lang.intentions.script.CopyLocalisationTextAsHtmlIntention`
  - `icu.windea.pls.lang.intentions.script.ConditionalSnippetToPropertyFormatIntention`
  - `icu.windea.pls.lang.intentions.script.ConditionalSnippetToBlockFormatIntention`

- Paradox Localisation（`language=PARADOX_LOCALISATION`，`categoryKey=intention.localisation.category`）
  - `icu.windea.pls.lang.intentions.localisation.ChangeLocalisationLocaleIntention`
  - `icu.windea.pls.lang.intentions.localisation.ChangeLocalisationColorIntention`
  - `icu.windea.pls.lang.intentions.localisation.CopyScriptedVariableNameIntention`
  - `icu.windea.pls.lang.intentions.localisation.CopyScriptedVariableLocalizedNameIntention`
  - `icu.windea.pls.lang.intentions.localisation.CopyDefinitionNameIntention`
  - `icu.windea.pls.lang.intentions.localisation.CopyDefinitionLocalizedNameIntention`
  - `icu.windea.pls.lang.intentions.localisation.CopyLocalisationNameIntention`
  - `icu.windea.pls.lang.intentions.localisation.CopyLocalisationTextIntention`
  - `icu.windea.pls.lang.intentions.localisation.CopyLocalisationTextAsPlainIntention`
  - `icu.windea.pls.lang.intentions.localisation.CopyLocalisationTextAsHtmlIntention`
  - `icu.windea.pls.lang.intentions.localisation.CopyLocalisationIntention`
  - `icu.windea.pls.lang.intentions.localisation.CopyLocalisationFromLocaleIntention`
  - `icu.windea.pls.lang.intentions.localisation.CopyLocalisationWithTranslationIntention`
  - `icu.windea.pls.lang.intentions.localisation.CopyLocalisationWithTranslationFromLocaleIntention`
  - `icu.windea.pls.lang.intentions.localisation.ReplaceLocalisationFromLocaleIntention`
  - `icu.windea.pls.lang.intentions.localisation.ReplaceLocalisationWithTranslationFromLocaleIntention`

- Paradox CSV（`language=PARADOX_CSV`，`categoryKey=intention.csv.category`）
  - `icu.windea.pls.lang.intentions.csv.QuoteIdentifierIntention`（`descriptionDirectoryName=CsvQuoteIdentifierIntention`）
  - `icu.windea.pls.lang.intentions.csv.UnquoteIdentifierIntention`（`descriptionDirectoryName=CsvUnquoteIdentifierIntention`）

---

## 二、分类与描述资源

- 分类键：
  - `intention.cwt.category`
  - `intention.script.category`
  - `intention.localisation.category`
  - `intention.csv.category`
- 描述资源：
  - 部分 intention 显式声明 `descriptionDirectoryName`（如 CWT/CSV 的 Quote/Unquote）。
  - 实现类位于 `icu.windea.pls.lang.intentions.<domain>` 命名空间（`cwt`/`script`/`localisation`/`csv`）。

---

## 三、功能意图概述（基于类名语义）

- CWT/CSV：标识符引号处理（`QuoteIdentifier` / `UnquoteIdentifier`）。
- Script：复制脚本变量/定义/本地化相关名称与文本；条件片段格式转换（`ConditionalSnippetTo*Format`）。
- Localisation：本地化文本的复制/替换（含来源 locale 与“带翻译”版本）、名称/颜色/语言切换等便捷操作。

> 具体触发条件与语义由对应 Kotlin 实现决定，本文件仅负责注册。

---

## 四、与其他 include 的关系

- 与 `pls-lang.xml`：共同构成编辑器交互体验（意图动作 + 高亮/补全/导航/重构）。
- 与 `pls-ep.xml`：意图动作通常依托语义层（定义/本地化/变量/作用域等）提供的解析与索引结果。

---

## 五、注意点与建议

- 部分 intention 配有描述目录；若需完善 UX，可为未声明者补充说明与示例。
- 本地化相关意图较多，建议与 `localisation` 语言设置与 Inlay 选项联动评估体验。

---

## 六、下一步建议

- 继续阅读：`pls-inspections.xml`（检查与 QuickFix 体系，覆盖面广）或 `pls-images.xml`（资源侧）。
