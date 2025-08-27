# 探索_5_pls-localisation.xml

> 时间：2025-08-27 16:06（本地）
> 来源文件：`src/main/resources/META-INF/pls-localisation.xml`
> 目的：解析 Paradox Localisation 语言的 IDE 支持声明（文件类型、解析、编辑器能力、模板、格式化、PSI 操作、浮动工具栏与动作、配色方案），为本地化文本的高效编写与维护提供索引。

---

## 一、语言与文件类型

- `fileType`：`language="PARADOX_LOCALISATION"`，`name="Paradox Localisation"`
  - `implementationClass`: `icu.windea.pls.localisation.ParadoxLocalisationFileType`
  - `fieldName`: `INSTANCE`
- `lang.parserDefinition`: `icu.windea.pls.localisation.ParadoxLocalisationParserDefinition`

> 结论：提供独立的本地化语言与解析定义，具备完整 PSI 支撑。

## 二、编辑/导航/提示能力（核心）

- 拼写检查：`spellchecker.support` → `...ParadoxLocalisationSpellchecker`
- 语法高亮：`lang.syntaxHighlighterFactory` → `...ParadoxLocalisationSyntaxHighlighterFactory`
- 配色页：`colorSettingsPage` → `...ParadoxLocalisationColorSettingsPage`
- 括号匹配：`lang.braceMatcher` → `...ParadoxLocalisationBraceMatcher`
- 注释器：`lang.commenter` → `...ParadoxLocalisationCommenter`
- 面包屑：`breadcrumbsInfoProvider` → `...ParadoxLocalisationBreadCrumbsProvider`
- 引号处理：`lang.quoteHandler` → `...ParadoxLocalisationQuoteHandler`
- 查找用法：`lang.findUsagesProvider` → `...ParadoxLocalisationFindUsagesProvider`
- 元素描述：`elementDescriptionProvider` → 同上
- 扩展选区：`extendWordSelectionHandler` → `...ParadoxLocalisationWordSelectionHandler`
- 语义标注：`annotator` → `...ParadoxLocalisationBasicAnnotator`
- 智能回车：`lang.smartEnterProcessor` → `...editorActions.smartEnter.ParadoxLocalisationSmartEnterProcessor`
- 折叠：`lang.foldingBuilder` → `...ParadoxLocalisationFoldingBuilder`
- 声明范围：`declarationRangeHandler`（key=`ParadoxLocalisationPropertyList`）→ `...ParadoxLocalisationDeclarationRangeHandler`
- 实现选区：`lang.implementationTextSelectioner` → `...ParadoxLocalisationImplementationTextSelectioner`
- 错误快速修复：`errorQuickFixProvider` → `...ParadoxLocalisationErrorQuickFixProvider`
- 语句上下移动：`statementUpDownMover` → `...ParadoxLocalisationMover`
- 解包操作：`lang.unwrapDescriptor` → `...ParadoxLocalisationUnwrapDescriptor`

> 结论：覆盖高亮/用法/结构/动作/智能编辑等全链路 IDE 能力，适配本地化文本的编辑场景。

## 三、代码样式与格式化

- `lang.formatter` → `...ParadoxLocalisationFormattingModelBuilder`
- `codeStyleSettingsProvider` → `...ParadoxLocalisationCodeStyleSettingsProvider`
- `langCodeStyleSettingsProvider` → `...ParadoxLocalisationLanguageCodeStyleSettingsProvider`

> 结论：支持独立的格式化与代码风格定制。

## 四、动态模板（Live Templates）

- `liveTemplateContext`：
  - `contextId="PARADOX_LOCALISATION"` → `...TemplateContextType$Base`
  - `contextId="PARADOX_LOCALISATION_LOCALISATION_TEXT"`，`baseContextId="PARADOX_LOCALISATION"` → `...$LocalisationText`
- `defaultLiveTemplates`：`/liveTemplates/ParadoxLocalisation.xml`

> 结论：提供基础与“本地化文本”场景的模板上下文，并内置默认模板集。

## 五、PSI/结构与导航

- `declarationRangeHandler`：界定属性列表声明范围（利于高亮/折叠/导航）。
- `fileStructureGroupRuleProvider`：`...ParadoxLocalisationLocaleFileStructureGroupRuleProvider`（用于文件结构分组，名称显示与 Locale 维度关联）。
- `navbar`：本地化导航栏增强。

## 六、浮动工具栏与动作（UI 增强）

- 浮动工具栏：
  - `customizableActionGroupProvider` → `...ParadoxLocalisationFloatingToolbarCustomizableGroupProvider`
  - `textEditorCustomizer` → `...ParadoxLocalisationTextEditorCustomizer`
- 动作与快捷键：
  - `Pls.ParadoxLocalisation.Styling.CreateReference`（Ctrl+Alt+R）→ 引用样式
  - `Pls.ParadoxLocalisation.Styling.CreateIcon`（Ctrl+Alt+I）→ 图标样式
  - `Pls.ParadoxLocalisation.Styling.CreateCommand`（Ctrl+Alt+C）→ 命令样式
  - `Pls.ParadoxLocalisation.Styling.SetColorGroup` 分组（searchable=false）
- 插入组：`Pls.ParadoxLocalisation.InsertGroup`
  - `add-to-group group-id="GenerateGroup"`（追加到默认 Generate 菜单）
  - 引用上述 3 个样式动作

> 结论：针对本地化文本提供专门的富文本/样式化编辑辅助，且融入 IDE 的 Generate 菜单与浮动工具栏。

## 七、配色方案（Additional Text Attributes）

- 多套主题适配：
  - `Default`/`Darcula`/`Darcula Contrast`/`High contrast`/`IntelliJ Light`/`Dark`/`Light`
  - 对应文件：`colorSchemes/ParadoxLocalisation*.xml`

> 结论：在不同主题下保持良好可读性与一致性。

## 八、与 README 的对应关系（校准）

- README 中关于“本地化”编辑体验（高亮/结构/模板/检查/动作）与此处声明高度一致；通过浮动工具栏与默认模板增强了富文本/样式化编辑体验。

## 九、开放问题与建议

- `fileType` 未在此声明扩展名；本地化常见扩展为 `.yml/.yaml`，可能在其他配置（如 `pls-lang.xml` 或 `pls-config.xml`）中绑定，后续文件中验证。
- 建议在文档增加“本地化样式动作”演示（富文本引用、图标、命令）与快捷键速查。

## 十、后续阅读方向（待你指定）

- 继续语言基础设施：`src/main/resources/META-INF/pls-csv.xml`
- 语言聚合/扩展点与检查：`src/main/resources/META-INF/pls-lang.xml`、`src/main/resources/META-INF/pls-ep.xml`、`src/main/resources/META-INF/pls-inspections.xml`
