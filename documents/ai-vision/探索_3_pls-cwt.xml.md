# 探索_3_pls-cwt.xml

> 时间：2025-08-27 15:51（本地）
> 来源文件：`src/main/resources/META-INF/pls-cwt.xml`
> 目的：解析 CWT 规则语言的 IDE 支持声明（文件类型、解析、编辑器能力、代码样式、模板、PSI 操作等），为后续 CWT 规则编写/导入/调试提供索引。

---

## 一、语言与文件类型

- `fileType`：`language="CWT"`，`name="Cwt"`，`extensions="cwt"`
  - `implementationClass`: `icu.windea.pls.cwt.CwtFileType`
  - `fieldName`: `INSTANCE`
- `lang.parserDefinition`: `icu.windea.pls.cwt.CwtParserDefinition`

> 结论：`.cwt` 为 CWT 规则文件的标准扩展名；具备独立的语言与解析定义，支持完整的 PSI 构建。

## 二、编辑/导航/提示能力（核心）

- 拼写检查：`spellchecker.support` → `icu.windea.pls.cwt.editor.CwtSpellchecker`
- 语法高亮：`lang.syntaxHighlighterFactory` → `icu.windea.pls.cwt.editor.CwtSyntaxHighlighterFactory`
- 配色页：`colorSettingsPage` → `icu.windea.pls.cwt.editor.CwtColorSettingsPage`
- 括号匹配：`lang.braceMatcher` → `icu.windea.pls.cwt.editor.CwtBraceMatcher`
- 引号处理：`lang.quoteHandler` → `icu.windea.pls.cwt.editor.CwtQuoteHandler`
- 查找用法：`lang.findUsagesProvider` → `icu.windea.pls.cwt.editor.CwtFindUsagesProvider`
- 元素描述：`elementDescriptionProvider` → `icu.windea.pls.cwt.editor.CwtFindUsagesProvider`
- 扩展选区：`extendWordSelectionHandler` → `icu.windea.pls.cwt.editor.CwtWordSelectionHandler`
- 语义标注：`annotator` → `icu.windea.pls.cwt.editor.CwtBasicAnnotator`
- 注释器：`lang.commenter` → `icu.windea.pls.cwt.editor.CwtCommenter`
- 面包屑：`breadcrumbsInfoProvider` → `icu.windea.pls.cwt.editor.CwtBreadCrumbsProvider`
- 折叠：`lang.foldingBuilder` → `icu.windea.pls.cwt.folding.CwtFoldingBuilder`
- 实现选区：`lang.implementationTextSelectioner` → `icu.windea.pls.cwt.codeInsight.CwtImplementationTextSelectioner`
- 上下移动语句：`statementUpDownMover` → `icu.windea.pls.cwt.codeInsight.editorActions.CwtMover`
- 智能回车：`lang.smartEnterProcessor` → `icu.windea.pls.cwt.codeInsight.editorActions.CwtSmartEnterProcessor`
- 导航栏：`navbar` → `icu.windea.pls.cwt.navigation.CwtNavBar`
- 结构视图：`lang.psiStructureViewFactory` → `icu.windea.pls.cwt.structureView.CwtStructureViewFactory`
- 围绕与解包：
  - `lang.surroundDescriptor` → `icu.windea.pls.cwt.codeInsight.surroundWith.CwtSurroundDescriptor`
  - `lang.unwrapDescriptor` → `icu.windea.pls.cwt.codeInsight.unwrap.CwtUnwrapDescriptor`

> 结论：CWT 语言具备完整的 IDE 级编辑体验（高亮/折叠/结构视图/查找用法/注释/面包屑/移动与回车动作等），便于高效维护复杂规则。

## 三、代码样式与格式化

- `lang.formatter` → `icu.windea.pls.cwt.formatter.CwtFormattingModelBuilder`
- `codeStyleSettingsProvider` → `icu.windea.pls.cwt.codeStyle.CwtCodeStyleSettingsProvider`
- `langCodeStyleSettingsProvider` → `icu.windea.pls.cwt.codeStyle.CwtLanguageCodeStyleSettingsProvider`

> 结论：支持独立的格式化/代码风格设置，可在 IDE 的 Code Style 与 Color Scheme 页面进行项目/全局级别的定制。

## 四、动态模板（Live Templates）

- `liveTemplateContext`：
  - `contextId="CWT"` → `icu.windea.pls.cwt.codeInsight.template.CwtTemplateContextType$Base`
  - `contextId="CWT_MEMBERS"`，`baseContextId="CWT"` → `...CwtTemplateContextType$Members`

> 结论：可为规则与成员定义提供上下文感知的模板片段，提升规则编写效率与一致性。

## 五、PSI 元素操作（重构友好）

- `lang.elementManipulator`：
  - for `icu.windea.pls.cwt.psi.CwtOptionKey` → `CwtOptionKeyManipulator`
  - for `icu.windea.pls.cwt.psi.CwtPropertyKey` → `CwtPropertyKeyManipulator`
  - for `icu.windea.pls.cwt.psi.CwtValue` → `CwtValueManipulator`
  - for `icu.windea.pls.cwt.psi.CwtString` → `CwtStringManipulator`

> 结论：就地编辑/重命名/粘贴等操作更稳健，为补全/导航/意图/重构提供 PSI 基础设施。

## 六、与 README 的对应关系（校准）

- README 强调“以 CWT 规则驱动语言能力，支持自定义与导入”；本文件提供了 CWT 规则语言的完整 IDE 支撑矩阵，是该承诺的基础设施。
- 推断：CWT 规则文件的质量直接影响脚本/本地化/CSV 等语言的补全/导航/检查等能力；因此 CWT 编辑体验至关重要。

## 七、开放问题与建议

- 是否存在 CWT 专属的 inspections/intentions？本文件未见，可能在 `pls-inspections.xml`、`pls-intentions.xml` 中体现。
- 建议在文档中补充“CWT 规则调试/观测”章节：如何定位某条规则的生效范围、冲突与覆盖关系。
- 建议提供示例 Live Templates（如规则段落骨架），并在文档中可视化展示。

## 八、后续阅读方向（待你指定）

- 语言聚合：`src/main/resources/META-INF/pls-lang.xml`
- 规则影响面：`src/main/resources/META-INF/pls-inspections.xml`（若含 CWT 相关检查）
- 具体语言域：`src/main/resources/META-INF/pls-script.xml` / `.../pls-localisation.xml` / `.../pls-csv.xml`
