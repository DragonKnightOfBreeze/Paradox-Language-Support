# 探索_4_pls-script.xml

> 时间：2025-08-27 16:00（本地）
> 来源文件：`src/main/resources/META-INF/pls-script.xml`
> 目的：解析 Paradox Script 语言的 IDE 支持声明（文件类型、解析、编辑器能力、代码样式、模板、PSI 操作与配色方案），为脚本类内容的开发提供索引。

---

## 一、语言与文件类型

- `fileType`：`language="PARADOX_SCRIPT"`，`name="Paradox Script"`
  - `implementationClass`: `icu.windea.pls.script.ParadoxScriptFileType`
  - `fieldName`: `INSTANCE`
  - `fileNames="descriptor.mod"`（绑定特定文件名；未在此文件声明扩展名）
- `lang.parserDefinition`: `icu.windea.pls.script.ParadoxScriptParserDefinition`

> 结论：对 `descriptor.mod` 文件提供脚本语言支持；脚本语法的 PSI/解析由独立解析器定义提供。

## 二、编辑/导航/提示能力（核心）

- 拼写检查：`spellchecker.support` → `icu.windea.pls.script.editor.ParadoxScriptSpellchecker`
- 语法高亮：`lang.syntaxHighlighterFactory` → `icu.windea.pls.script.editor.ParadoxScriptSyntaxHighlighterFactory`
- 配色页：`colorSettingsPage` → `icu.windea.pls.script.editor.ParadoxScriptColorSettingsPage`
- 括号匹配：`lang.braceMatcher` → `icu.windea.pls.script.editor.ParadoxScriptBraceMatcher`
- 折叠：`lang.foldingBuilder` → `icu.windea.pls.script.folding.ParadoxScriptFoldingBuilder`
- 注释器：`lang.commenter` → `icu.windea.pls.script.editor.ParadoxScriptCommenter`
- 面包屑：`breadcrumbsInfoProvider` → `icu.windea.pls.script.editor.ParadoxScriptBreadCrumbsProvider`
- 颜色提供：`colorProvider` → `icu.windea.pls.script.editor.ParadoxScriptColorProvider`
- 引号处理：`lang.quoteHandler` → `icu.windea.pls.script.editor.ParadoxScriptQuoteHandler`
- 查找用法：`lang.findUsagesProvider` → `icu.windea.pls.script.editor.ParadoxScriptFindUsagesProvider`
- 元素描述：`elementDescriptionProvider` → `icu.windea.pls.script.editor.ParadoxScriptFindUsagesProvider`
- 扩展选区：`extendWordSelectionHandler` → `icu.windea.pls.script.editor.ParadoxScriptWordSelectionHandler`
- 语义标注：`annotator` → `icu.windea.pls.script.editor.ParadoxScriptBasicAnnotator`
- 实现选区：`lang.implementationTextSelectioner` → `icu.windea.pls.script.codeInsight.ParadoxScriptImplementationTextSelectioner`
- 错误快速修复：`errorQuickFixProvider` → `icu.windea.pls.script.codeInsight.ParadoxScriptErrorQuickFixProvider`
- 智能回车：`lang.smartEnterProcessor` → `icu.windea.pls.script.codeInsight.editorActions.ParadoxScriptSmartEnterProcessor`
- 上下移动语句：`statementUpDownMover` → `icu.windea.pls.script.codeInsight.editorActions.ParadoxScriptMover`
- 导航栏：`navbar` → `icu.windea.pls.script.navigation.ParadoxScriptNavBar`
- 结构视图：`lang.psiStructureViewFactory` → `icu.windea.pls.script.structureView.ParadoxScriptStructureViewFactory`
- 文件结构分组规则：`fileStructureGroupRuleProvider` → `icu.windea.pls.script.usages.ParadoxDefinitionFileStructureGroupRuleProvider`
- 围绕与解包：
  - `lang.surroundDescriptor` → `icu.windea.pls.script.codeInsight.surroundWith.ParadoxScriptSurroundDescriptor`
  - `lang.unwrapDescriptor` → `icu.windea.pls.script.codeInsight.unwrap.ParadoxScriptUnwrapDescriptor`

> 结论：脚本语言提供工业级 IDE 体验，覆盖高亮/折叠/查找用法/结构视图/面包屑/快速修复/编辑动作等全链路。

## 三、代码样式与格式化

- `lang.formatter` → `icu.windea.pls.script.formatter.ParadoxScriptFormattingModelBuilder`
- `codeStyleSettingsProvider` → `icu.windea.pls.script.codeStyle.ParadoxScriptCodeStyleSettingsProvider`
- `langCodeStyleSettingsProvider` → `icu.windea.pls.script.codeStyle.ParadoxScriptLanguageCodeStyleSettingsProvider`

> 结论：提供脚本语言独立的格式化与风格设定接口。

## 四、动态模板（Live Templates）

- `liveTemplateContext`：
  - `contextId="PARADOX_SCRIPT"` → `...ParadoxScriptTemplateContextType$Base`
  - `contextId="PARADOX_SCRIPT_MEMBERS"`，`baseContextId="PARADOX_SCRIPT"` → `...$Members`
  - `contextId="PARADOX_SCRIPT_INLINE_MATH_EXPRESSIONS"`，`baseContextId="PARADOX_SCRIPT"` → `...$InlineMathExpressions`

> 结论：覆盖基础、成员以及“内联数学表达式”场景的模板上下文，利于规则与表达式片段复用。

## 五、PSI 元素操作（重构友好）

- `lang.elementManipulator`：
  - for `ParadoxScriptPropertyKey` → `ParadoxScriptPropertyKeyManipulator`
  - for `ParadoxScriptValue` → `ParadoxScriptValueManipulator`
  - for `ParadoxScriptString` → `ParadoxScriptStringManipulator`
  - for `ParadoxScriptParameter` → `ParadoxScriptParameterManipulator`
  - for `ParadoxScriptInlineMathParameter` → `ParadoxScriptInlineMathParameterManipulator`

> 结论：就地编辑/重命名/重构具备良好 PSI 支持，保障补全/导航/意图等高级能力的稳定性。

## 六、配色方案（Additional Text Attributes）

- 为多套主题提供附加配色：
  - `Default` → `colorSchemes/ParadoxScriptDefault.xml`
  - `Darcula` → `colorSchemes/ParadoxScriptDarcula.xml`
  - `Darcula Contrast` → `colorSchemes/ParadoxScriptDarculaContrast.xml`
  - `High contrast` → `colorSchemes/ParadoxScriptHighContrast.xml`
  - `IntelliJ Light` → `colorSchemes/ParadoxScriptIntelliJLight.xml`
  - `Dark` → `colorSchemes/ParadoxScriptDark.xml`
  - `Light` → `colorSchemes/ParadoxScriptLight.xml`

> 结论：脚本语言具备主题化适配，提升不同 UI 模式下的可读性与一致性。

## 七、与 README 的对应关系（校准）

- README 中关于“脚本语言”的高亮、导航、结构、检查、模板、格式化等能力，与此文件的声明一一映射；并额外提供错误快速修复与内联数学表达式模板上下文。

## 八、开放问题与建议

- fileType 当前仅通过 `fileNames="descriptor.mod"` 绑定；其他脚本文件的识别可能在别处配置（例如语言聚合或配置模块），可在后续文件中验证。
- 建议在文档补充示例模板（如触发器、效果、定义等片段），并说明与 CWT 规则驱动的联动关系。

## 九、后续阅读方向（待你指定）

- 同为语言基础设施：`src/main/resources/META-INF/pls-localisation.xml`、`src/main/resources/META-INF/pls-csv.xml`
- 语言聚合/扩展点：`src/main/resources/META-INF/pls-lang.xml`、`src/main/resources/META-INF/pls-ep.xml`
