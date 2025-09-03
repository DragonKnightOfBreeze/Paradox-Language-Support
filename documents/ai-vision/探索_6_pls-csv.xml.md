# 探索_6_pls-csv.xml

> 时间：2025-08-27 16:15（本地）
> 来源文件：`src/main/resources/META-INF/pls-csv.xml`
> 目的：解析 Paradox CSV 语言的 IDE 支持声明（文件类型、解析、编辑器能力、结构视图、代码风格、PSI 操作与配色），为 CSV 数据的高效编辑提供索引。

---

## 一、语言与文件类型

- `fileType`：`language="PARADOX_CSV"`，`name="Paradox Csv"`
  - `implementationClass`: `icu.windea.pls.csv.ParadoxCsvFileType`
  - `fieldName`: `INSTANCE`
- `lang.parserDefinition`: `icu.windea.pls.csv.ParadoxCsvParserDefinition`

> 结论：定义独立 CSV 语言及文件类型，并提供解析定义以生成 PSI。

## 二、编辑/导航/提示能力（核心）

- 拼写检查：`spellchecker.support` → `...csv.editor.ParadoxCsvSpellchecker`
- 语法高亮：`lang.syntaxHighlighterFactory` → `...csv.editor.ParadoxCsvSyntaxHighlighterFactory`
- 配色页：`colorSettingsPage` → `...csv.editor.ParadoxCsvColorSettingsPage`
- 注释器：`lang.commenter` → `...csv.editor.ParadoxCsvCommenter`
- 面包屑：`breadcrumbsInfoProvider` → `...csv.editor.ParadoxCsvBreadCrumbsProvider`
- 引号处理：`lang.quoteHandler` → `...csv.editor.ParadoxCsvQuoteHandler`
- 查找用法：`lang.findUsagesProvider` → `...csv.editor.ParadoxCsvFindUsagesProvider`
- 元素描述：`elementDescriptionProvider` → 复用 `ParadoxCsvFindUsagesProvider`
- 扩展选区：`extendWordSelectionHandler` → `...csv.editor.ParadoxCsvWordSelectionHandler`
- 语义标注：`annotator` → `...csv.editor.ParadoxCsvBasicAnnotator`
- 引用高亮：`highlightUsagesHandlerFactory` → `...csv.editor.ParadoxCsvHighlightUsagesHandlerFactory`
- 折叠：`lang.foldingBuilder` → `...csv.folding.ParadoxCsvFoldingBuilder`
- 声明范围：`declarationRangeHandler`（key=`ParadoxCsvRow`）→ `...csv.codeInsight.ParadoxCsvDeclarationRangeHandler`

> 结论：覆盖高亮/用法/结构/折叠/选区/拼写等基础 IDE 能力，面向 CSV 行列元素进行优化。

## 三、代码样式与格式化

- `codeStyleSettingsProvider` → `...csv.codeStyle.ParadoxCsvCodeStyleSettingsProvider`
- `langCodeStyleSettingsProvider` → `...csv.codeStyle.ParadoxCsvLanguageCodeStyleSettingsProvider`

> 备注：本文件未见显式 `lang.formatter`，格式化行为可能由代码风格设置或通用机制配合完成。

## 四、结构与导航

- `navbar`：`...csv.navigation.ParadoxCsvNavBar`
- `lang.psiStructureViewFactory`：`...csv.structureView.ParadoxCsvStructureViewFactory`

> 结论：提供文件导航栏与结构视图，利于按行/列组织浏览。

## 五、PSI/元素操控器

- `lang.elementManipulator`：`forClass="...csv.psi.ParadoxCsvColumn"` → `...csv.psi.ParadoxCsvColumnManipulator`

> 结论：支持对“列”元素的文本区间读写与重命名等操作，便于重构与编辑。

## 六、模板与 UI 动作

- 本文件未声明 `liveTemplateContext`/`defaultLiveTemplates`。
- 未见浮动工具栏/自定义动作相关声明。

> 结论：CSV 以基础编辑体验为主，无专用模板/动作。

## 七、配色方案（Color Settings）

- 提供 `colorSettingsPage`，用于在设置页展示示例与可配置项。
- 未见每主题的 `additionalTextAttributes` 独立声明（可能不需要）。

## 八、与 README 的对应关系（校准）

- 与 README 对 CSV 的概述一致：提供基础语言支持（高亮、结构视图、用法、折叠、代码风格等）。

## 九、开放问题与建议

- 扩展名关联未在此 XML 中体现（通常由 `FileType` 实现类或其他配置提供）。
- 建议文档层面补充：列对齐/分隔符可视化/列高亮切换等操作指南（若已支持）。

## 十、后续阅读方向（待你指定）

- 语言聚合与通用配置：`src/main/resources/META-INF/pls-lang.xml`
- 扩展点与检查：`src/main/resources/META-INF/pls-ep.xml`、`src/main/resources/META-INF/pls-inspections.xml`
