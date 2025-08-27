# 探索_11_pls-inspections.xml

> 位置：`src/main/resources/META-INF/pls-inspections.xml`
> 资源包：`messages.PlsBundle`
> 抑制器：`ParadoxScriptInspectionSuppressor` / `ParadoxLocalisationInspectionSuppressor` / `ParadoxCsvInspectionSuppressor`

---

## 概览

- 分类维度：按语言域和主题分组（common/bug/scope/expression/event/inference），通过 `groupKey` 和 `groupPathKey` 组织到 IDE 检查树。
- 抑制支持：为 Script/Localisation/CSV 注册 `lang.inspectionSuppressor`，允许基于代码/注释抑制。
- 计数（不含抑制器）：
  - Overridden：5
  - Paradox Script：40（Common 17 / Bug 6 / Scope 3 / Expression 6 / Events 5 / Inference 3）
  - Paradox Localisation：20（Common 13 / Bug 2 / Scope 3 / Expression 2）
  - Paradox CSV：6
  - Lints：1（Tiger）
- 严重级别与默认开关：
  - 结构/解析类问题多为 ERROR；语义与风格多为 WARNING / WEAK WARNING。
  - 若干高噪点项默认关闭：`UnusedDynamicValue`、`UnsetDynamicValue`、`ConflictingScopeContextInference`、以及部分 Overridden 提示。

---

## Overridden（文件/脚本元素覆写）

- `ParadoxOverriddenForFile`（WEAK WARNING，默认关）
- `ParadoxOverriddenForScriptedVariable`（WEAK WARNING，默认关）
- `ParadoxOverriddenForDefinition`（WEAK WARNING，默认关）
- `ParadoxIncorrectOverriddenForScriptedVariable`（WARNING）
- `ParadoxIncorrectOverriddenForDefinition`（WARNING）
- 包：`icu.windea.pls.lang.inspections.overridden.*`

---

## Paradox Script

- Common（groupKey=`inspection.script.common.group`）：
  - DuplicateScriptedVariables（WARNING）
  - IncorrectFileEncoding（WARNING）
  - UnresolvedExpression（ERROR）
  - ConflictingResolvedExpression（ERROR）
  - MissingExpression（ERROR）
  - TooManyExpression（WEAK WARNING）
  - IncorrectExpression（WARNING）
  - MissingLocalisation（WARNING）
  - MissingImage（WARNING）
  - UnresolvedScriptedVariable（ERROR）
  - UnresolvedPathReference（ERROR）
  - UnusedInlineScript（WEAK WARNING）
  - UnusedParameter（WEAK WARNING）
  - MissingParameter（WARNING）
  - UnusedDynamicValue（WEAK WARNING，默认关）
  - UnsetDynamicValue（WEAK WARNING，默认关）
  - IncorrectPathReference（WARNING）
- Possible Bugs（groupKey=`inspection.script.bug.group`）：
  - IncorrectSyntax / UnsupportedParameterUsage / UnsupportedParameterCondition / UnsupportedInlineMath / UnsupportedInlineScriptUsage / UnsupportedRecursion（均 WARNING）
- Scope issues（groupKey=`inspection.script.scope.group`）：
  - IncorrectScope / IncorrectScopeSwitch（WARNING），IncorrectScopeLinkChain（WEAK WARNING）
- Complex expressions（groupKey=`inspection.script.expression.group`）：
  - IncorrectScopeFieldExpression / IncorrectValueFieldExpression / IncorrectVariableFieldExpression / IncorrectDynamicValueExpression / IncorrectDatabaseObjectExpression / IncorrectDefineReferenceExpression（均 ERROR）
- Events（groupKey=`inspection.script.event.group`）：
  - IncorrectEventId / IncorrectEventNamespace / MissingEventNamespace / MismatchedEventId / NonTriggeredEvent（均 WARNING）
- Smart inference（groupKey=`inspection.script.inference.group`）：
  - ConflictingInlineScriptUsage / RecursiveInlineScriptUsage（WARNING）
  - ConflictingScopeContextInference（WARNING，默认关）

---

## Paradox Localisation

- Common（groupKey=`inspection.localisation.common.group`）：
  - DuplicateProperties（WARNING）
  - IncorrectFileName（WARNING）
  - IncorrectFileEncoding（WARNING）
  - MultipleLocales（WARNING）
  - MissingLocale（ERROR）
  - MissingLocalisation（ERROR）
  - UnsupportedLocale（ERROR）
  - UnresolvedColor（ERROR）
  - UnresolvedScriptedVariable（ERROR）
  - UnresolvedIcon（ERROR）
  - UnresolvedConcept（ERROR）
  - UnresolvedTextFormat（ERROR）
  - UnresolvedTextIcon（ERROR）
- Possible bugs（groupKey=`inspection.localisation.bug.group`）：
  - IncorrectSyntax / UnsupportedRecursion（WARNING）
- Scope issues（groupKey=`inspection.localisation.scope.group`）：
  - IncorrectScope / IncorrectScopeSwitch（WARNING），IncorrectScopeLinkChain（WEAK WARNING）
- Complex expressions（groupKey=`inspection.localisation.expression.group`）：
  - IncorrectCommandExpression / IncorrectDatabaseObjectExpression（ERROR）

---

## Paradox CSV

- Common（groupKey=`inspection.csv.common.group`）：
  - MissingColumns（ERROR）
  - DuplicateColumns（WARNING）
  - UnresolvedColumns（ERROR）
  - IncorrectColumnSize（级别未在标签内声明，见下“格式问题”）
  - UnresolvedExpression（ERROR）
  - IncorrectExpression（ERROR）
- 格式问题：文件在 `IncorrectColumnSizeInspection` 之后出现游离的行：
  ```xml
  enabledByDefault="true" level="ERROR"
  ```
  疑为误排版，应属于上一条 inspection 的属性。建议修正为：将该属性移入 `IncorrectColumnSizeInspection` 的 `<localInspection ... />` 标签内，或按预期级别在该条上明确声明。

---

## Linting Tools（外部 Lint）

- `externalAnnotator`：`PlsTigerLintAnnotator`（language=`PARADOX`）
- `localInspection`：`PlsTigerLint`（ERROR，默认开）
- 关联：项目包含 `references/tiger/tiger-result.json`，推测与 Tiger 结果集成以呈现 IDE 告警。

---

## 与其它配置的关系与作用域

- 与 `pls-ep.xml`：多类检查依托表达式解析/匹配/支持等 EP 的语义结果；Suppressor 与“检查与抑制”扩展相呼应。
- 与 `pls-lang.xml`：作为编辑体验的一环，与导航/高亮/重构/Intention 共同构建“问题定位—修复建议—自动修复”的闭环。

---

## 观察与建议

- 噪点控制：保持默认关闭的高噪点检查，必要时在设置中逐项启用。
- 事件检查可扩展：`NonTriggeredEvent` 等可结合索引进一步降低误报。
- CSV 配置建议修复：处理“游离的 `enabledByDefault/level` 行”，以免加载时产生潜在解析问题。
- Lint 集成：若 Tiger 结果较大，建议为 Lint 提供分组/过滤与开关项，减轻编辑器负担。

---

## 参考实现包路径

- Script：`icu.windea.pls.lang.inspections.script.*`
- Localisation：`icu.windea.pls.lang.inspections.localisation.*`
- CSV：`icu.windea.pls.lang.inspections.csv.*`
- Overridden：`icu.windea.pls.lang.inspections.overridden.*`
- Lints：`icu.windea.pls.lang.inspections.lints.*`
