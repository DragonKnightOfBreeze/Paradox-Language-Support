# 拼写检查机制设计

> - 版本：`v3.1.0`
> - 更新日期：`2026-11-28`
> - 状态：**现行活规范（Living Specification）**
> - 文档定位：规范四门语言在文法级别参与拼写检查的 PSI 元素、词元拆分策略与当前排除边界。

---

## 概述

关键 EP：`com.intellij.spellchecker.support`

策略基类：`com.intellij.spellchecker.tokenizer.SpellcheckingStrategy`

四种语言分别注册 `*SpellcheckerStrategy`（`CWT`、`PARADOX_SCRIPT`、`PARADOX_LOCALISATION`、`PARADOX_CSV`），均实现 `DumbAware`。

## 策略与原则

- 仅作文法级别的拼写检查，不作语义级别的拼写检查。
- 跳过空白（`PsiWhiteSpace`）与存在语言注入的位置（`isInjectedLanguageFragment`）。
- 单行注释等 `PsiComment` 委托到 `super.getTokenizer(element)`；CWT 的选项注释 `CwtOptionComment` 直接返回 `EMPTY_TOKENIZER`。
- 包含空白的键/值/表达式使用 `PlainTextSplitter`，不包含空白的标识符使用 `IdentifierSplitter`；CWT 与 CSV 通过 `BlankAwareTokenizer` 统一处理。
- `TokenAwareTokenizer` 仅消费与预期词元类型匹配的子词元，要求文本为标识符，并使用 `IdentifierSplitter` 拆分。
- 除上述显式处理外，其余元素返回 `EMPTY_TOKENIZER`。

## 按语言划分的行为

| 语言 | PSI 元素 | 词元化器 | 说明 |
| :--- | :--- | :--- | :--- |
| CWT | `CwtOptionComment` | `EMPTY_TOKENIZER` | 选项注释不参与检查 |
| CWT | `PsiComment` | `super` | 委托平台默认词元化器 |
| CWT | `CwtOptionKey` / `CwtStringExpressionElement` | `BlankAwareTokenizer` | 含空白用 `PlainTextSplitter`，否则用 `IdentifierSplitter` |
| Paradox Script | `ParadoxScriptScriptedVariableName` | `TokenAwareTokenizer(SCRIPTED_VARIABLE_NAME_TOKEN)` | 封装变量名 |
| Paradox Script | `ParadoxScriptedVariableReference` | `TokenAwareTokenizer(SCRIPTED_VARIABLE_REFERENCE_TOKEN)` | 封装变量引用 |
| Paradox Script | `ParadoxScriptParameter` | `TokenAwareTokenizer(PARAMETER_TOKEN)` | 脚本参数参与检查 |
| Paradox Script | `ParadoxScriptConditionParameter` | `TokenAwareTokenizer(CONDITION_PARAMETER_TOKEN)` | 条件参数参与检查 |
| Paradox Script | `ParadoxScriptArgument` | `TokenAwareTokenizer(ARGUMENT_TOKEN)` | 参数实参参与检查 |
| Paradox Script | `ParadoxScriptStringExpressionElement` | `EMPTY_TOKENIZER` | 目前不做检查 |
| Paradox Localisation | `ParadoxLocalisationText` | `TEXT_TOKENIZER` | 本地化文本 |
| Paradox Localisation | `ParadoxLocalisationPropertyKey` | `EMPTY_TOKENIZER` | 目前不做检查 |
| Paradox Localisation | `ParadoxLocalisationParameter` | `EMPTY_TOKENIZER` | 唯一排除的参数类型 |
| Paradox Localisation | `ParadoxScriptedVariableReference` | `TokenAwareTokenizer(SCRIPTED_VARIABLE_REFERENCE_TOKEN)` | 封装变量引用 |
| Paradox Localisation | `ParadoxLocalisationIcon` | `TokenAwareTokenizer(ICON_TOKEN)` | 图标引用 |
| Paradox Localisation | `ParadoxLocalisationTextIcon` | `TokenAwareTokenizer(TEXT_ICON_TOKEN)` | 文本图标引用 |
| Paradox Localisation | `ParadoxLocalisationTextFormat` | `TokenAwareTokenizer(TEXT_FORMAT_TOKEN)` | 文本格式引用 |
| Paradox CSV | `ParadoxCsvExpressionElement` | `BlankAwareTokenizer` | 含空白用 `PlainTextSplitter`，否则用 `IdentifierSplitter` |

补充说明：

- “排除参数”仅适用于本地化参数（`ParadoxLocalisationParameter`）。
- 脚本参数、条件参数与参数实参均使用 `TokenAwareTokenizer` + `IdentifierSplitter` 参与拼写检查，其中参数实参使用 `ARGUMENT_TOKEN`。
- 各语言未列出的元素（以及 `PsiWhiteSpace`、注入片段）统一返回 `EMPTY_TOKENIZER`。
