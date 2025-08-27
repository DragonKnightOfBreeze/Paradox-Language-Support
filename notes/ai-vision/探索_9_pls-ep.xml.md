# 探索_9_pls-ep.xml

> 时间：2025-08-27 16:39（本地）
> 来源文件：`src/main/resources/META-INF/pls-ep.xml`
> 目的：定义 PLS 专用扩展点（EP）与默认实现，支撑表达式/配置上下文/索引/元数据/图标/修饰符/参数/作用域等语义能力，并供第三方扩展。

---

## 一、扩展点定义总览（命名空间：`icu.windea.pls`）

- 可视化与提示
  - `colorProvider` → `ParadoxColorProvider`
  - `quickDocTextProvider` → `ParadoxQuickDocTextProvider`
  - `hintTextProvider` → `ParadoxHintTextProvider`
  - `referenceLinkProvider` → `ReferenceLinkProvider`
- CWT 配置生态
  - `injectedConfigProvider`、`overriddenConfigProvider`、`relatedConfigProvider`
  - `configContextProvider`、`declarationConfigContextProvider`
  - `configGroupDataProvider`、`configGroupFileProvider`
- 表达式与求值
  - `dataExpressionResolver`、`dataExpressionMerger`、`dataExpressionPriorityProvider`
  - `scriptExpressionMatcher`、`csvExpressionMatcher`
  - `scriptExpressionSupport`、`localisationExpressionSupport`、`csvExpressionSupport`
  - `pathReferenceExpressionSupport`
- 图标与索引
  - `localisationIconSupport`
  - `infoIndexSupport`
- 检查与抑制
  - `incorrectExpressionChecker`
  - `definitionInspectionSuppressionProvider`
- 元数据与推断
  - `inferredGameTypeProvider`、`metadataProvider`
- 修饰符体系
  - `modifierSupport`、`modifierNameDescProvider`、`modifierIconProvider`、`definitionModifierProvider`
- 参数体系
  - `parameterSupport`、`parameterInferredConfigProvider`、`localisationParameterSupport`
- 作用域体系
  - `definitionScopeContextProvider`、`definitionInferredScopeContextProvider`、`definitionSupportedScopesProvider`
  - `dynamicValueScopeContextProvider`、`dynamicValueInferredScopeContextProvider`
  - `overriddenScopeContextProvider`
- 呈现/继承/内联/优先级
  - `definitionPresentationProvider`、`definitionInheritSupport`、`inlineSupport`、`priorityProvider`
- 工具（Import/Export）
  - `modImporter`、`modExporter`

> 均标注 `dynamic="true"`：支持运行时动态加载/卸载扩展。

---

## 二、默认实现注册（精选分组）

- Color/QuickDoc/HintText
  - 颜色：`ParadoxScriptStringColorProvider`、`ParadoxScriptBlockColorProvider`、`ParadoxScriptColorColorProvider`
  - Quick Doc：扩展脚本变量/定义/规则/on_action/复杂枚举值/动态值/参数等文本增强
  - Hint Text：定义、推断脚本变量/复杂枚举值/动态值，及扩展实体提示
- 引用链接（ReferenceLinkProvider）
  - CWT 配置、脚本变量、定义、本地化、文件路径、修饰符
- CWT 配置相关
  - 注入：`CwtTechnologyWithLevelInjectedConfigProvider`、`CwtInOnActionInjectedConfigProvider`
  - 覆盖：`CwtSwitchOverriddenConfigProvider`、`CwtTriggerWithParametersAwareOverriddenConfigProvider`
  - 关联：Base/InComplexExpression/Extended/Column
  - 上下文：`BaseCwtConfigContextProvider`（order="last"）、InlineScriptUsage/InlineScript/ParameterValue
  - 声明上下文：Base（order="last"）、GameRule、OnAction
  - 组数据/文件：Predefined（order="first"）/FileBased/Computed（order="last"）；BuiltIn（order="first"）/Remote/Local/Project
- 表达式
  - Data Resolver：`base`、`core`、`template`（order="last, after constant"）、`ant`、`re`、`constant`（order="last"）
  - Merger：`DefaultCwtDataExpressionMerger`
  - Priority：Base/Core
  - Script Matcher：Base/Core/Template/Ant/Regex/Constant（order="first"）
  - CSV Matcher：Base/Core
  - Script Support：本地化/同步/内联/定义/路径/枚举值/修饰符/别名名/常量/模板/动态值/Scope/Value/Variable 字段/数据库对象/define 引用/参数/本地化参数/科技等级等
  - Localisation Support：Command、DatabaseObject
  - CSV Support：Bool/Definition/EnumValue
  - 路径引用 Support：Icon/FilePath/FileName
- 索引信息（InfoIndexSupport）
  - 复杂枚举值、动态值、参数、本地化参数、带推断作用域的定义、事件与 on_action 互相关联索引
- 检查/抑制
  - 不正确表达式检查：区间整型/浮点、颜色字段、作用域/作用域组字段、科技等级、switch 中 trigger、带参数 trigger 中 trigger
  - 定义检查抑制：基础与 Stellaris 专用
- 元数据/推断
  - 推断游戏类型：基于游戏数据 ModPath、Workshop 路径
  - Metadata：基于 Launcher 设置、Mod 描述文件（order="last"）、Mod 元数据
- 修饰符体系
  - Support：预定义/模板/经济类别；Name/Desc Provider：Base；Icon Provider：Base/JobBased/EconomicCategoryBased
  - DefinitionModifierProvider：`StellarisScriptedModifierDefinitionModifierProvider`
- 参数/作用域
  - 参数 Support：Definition、ScriptValueInline、InlineScript
  - 参数推断配置：Default（order="last"）、Base、ComplexExpressionNode
  - 本地化参数 Support：Base
  - 定义作用域上下文：base/core（含 order），GameRule、OnAction；推断：Base、事件-动作互相关联系列
  - 动态值作用域：Base（含推断，order="last"）；覆盖：switch/trigger-with-params aware
- 呈现/继承/内联/优先级
  - DefinitionPresentationProvider：`StellarisTechnologyPresentation$Provider`
  - InheritSupport：`ParadoxSwappedTypeInheritSupport`、`StellarisEventInheritSupport`
  - InlineSupport：`ParadoxInlineScriptInlineSupport`
  - PriorityProvider：`ParadoxBasePriorityProvider`
- 模组导入/导出
  - Importer：`ParadoxDlcLoadImporter`、`ParadoxLauncherJsonV3Importer`
  - Exporter：`ParadoxLauncherJsonV2Exporter`、`ParadoxLauncherJsonV3Exporter`

---

## 三、执行顺序与选择策略

- `order="first/last/after ..."`：控制 Provider 链的执行前后次序。
- `id`（如 Data Resolver）：用于选择/覆盖特定策略；`constant` 往往最后执行、`template` 在其后（after constant）。
- 全部 EP `dynamic=true`：允许运行时扩展（如第三方游戏/模组适配插件）挂载/卸载，不需重启 IDE。

---

## 四、与其他 include 的关系

- `pls-lang.xml`：注册 IntelliJ 平台扩展（Completion/Annotator/Index/Refactoring/Actions 等）。
- `pls-ep.xml`：定义 PLS 自有 EP 与默认实现，是“语义/规则/上下文/索引/元数据”的抽象层与扩展点入口。
- `pls-inspections.xml`：具体的检查/快速修复实现与 UI 注册，依赖此处的 Checker/Support 能力。

---

## 五、注意点与建议

- 重度依赖顺序的 Provider（如 Resolver/Priority/ConfigGroup*）需保持 `order` 正确，避免覆盖不生效或链路短路。
- Stellaris 专项实现（`Stellaris*`）体现“按游戏类型分层”的设计；第三方扩展可按需新增实现。
- 引用/表达式生态较复杂，建议在单测中覆盖典型：常量/模板/正则/Ant/路径引用/参数推断/作用域推断等组合场景。

---

## 六、下一步建议

- 继续阅读：`src/main/resources/META-INF/pls-inspections.xml` 或 `pls-intentions.xml`（与 EP 能力紧密相关）。
