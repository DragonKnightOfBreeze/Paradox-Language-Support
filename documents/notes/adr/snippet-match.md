# 支持片段匹配

> - 模块：后端 > 规则系统
> - 版本：v3.0.4
> - 日期：2026-10-07
> - 状态：已实施（初步支持，告一段落）
> - 相关问题：[#340](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/340) [#413](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/413)

通常来说，一个表达式文本如果表示一个本地化引用，那么其文本就是完全匹配的。然而，在少数情况下，其文本会是完整的引用文本在加上特定的前后缀之后的形式，或是完整的引用文本在去除特定的前后缀之后的形式。对于前者，可以使用“模板/template”数据类型来表示，例如 `job_<job>_add`。对于后者，插件目前并未提供很好的支持。尽管插件目前已经提供了一类“后缀感知/suffixAware”的数据类型，但是目前发现，显然并不总是使用后缀。如何命名这种区别于“模板”的形式是一个微妙的问题，以前曾经考虑使用“位置感知/locationAware”，因为这与定义展示的位置表达式的一些特殊形式（如 `$_desc`）有些相像。然而，这个命名方案不够直观，并且两者在细节上终究存在一些区别。目前认为，“片段/snippet”是一个比较直观的命名方式，这也对齐了作为“模板表达式”中的各个部分的“片段”。

因此，需要新增数据类型种类“片段/snippet”，目前包括“定义引用片段/definitionReferenceSnippet”和“本地化引用片段/localisationReferenceSnippet”。

引用片段的文本通常作为某种具体引用的完整文本的一 部分。引用片段在加上特定的前缀和后缀（可能仅存在前缀或后缀）后才会等同于引用的完整文本。并且，引用片段可能对应多个引用，但在语义匹配阶段并不要求这些引用全部存在。

定义引用片段的数据类型：
- 名字：`DefinitionSnippet`
- 数据表达式的示例：`<entity>|$_a,b_$,c_$_d`
- 匹配的表达式的示例：`test`
- 匹配条件：同时存在类型为 `entity` 的定义 `test_a`、`b_test` 和 `c_test_d`

本地化引用片段的数据类型：
- 名字：`LocalisationSnippet`
- 数据表达式的示例：`localisation|$_desc,$_effect`
- 匹配的表达式的示例：`test`
- 匹配条件：同时存在本地化条目 `test_desc` 和 `test_effect`

备注：
- 考虑到这并非完整匹配，通过引用解析得到的应是一个 lightElement（例如 `ParadoxDefinitionSnippetLightElement`），类似参数的处理方式，而非直接解析为对应的定义或本地化。就初步支持而言，可以仅通过快速文档提示与对应的定义或本地化之间的相关关系。
- 采用宽松策略，在语义匹配阶段不要求存在所有对应的引用，通过代码检查 `IncorrectExressionInspection` 报告未完全匹配的问题。
- 无论是在语义匹配阶段，还是在代码检查阶段，匹配定义时都不忽略子类型。
- 对于 `$_desc` 这样的带简单占位符的模板表达式，提取为 `UnaryTemplateInfo`（实现自 `TemplateInfo`），放到 `icu.windea.pls.core.util`。
- 通过工厂模式从字符串创建 `RangeInfo` 和 `TemplateInfo`。

任务（step 1）：
- [x] `RangeInfo` 和 `TemplateInfo` 的相关更新
  - [x] `icu.windea.pls.core.util.IntRangeInfo` `icu.windea.pls.core.util.FloatRangeInfo`
    - 提供静态工厂方法 `create(expression)` + 提供去重方法 `normalize()`
  - [x] `icu.windea.pls.core.util.TemplateInfo`
    - 带类型参数 `T`，属性 `expression: String`，方法 `extract(text: String): T?` `resolve(args: T): String`
  - [x] `icu.windea.pls.core.util.UnaryTemplateInfo`
    - 数据类，构造属性包括 `expression: String` `placeholder: String = "$"`
    - 提供静态工厂方法 `create(expression, placeholder)` + 提供去重方法 `normalize()`
  - [x] ~~`icu.windea.pls.core.util.NamedTemplateInfo`~~
    - 由于目前并未使用，仅作记录，不需要真正实现
- [x] `CwtDataType` 的相关更新
  - [x] `icu.windea.pls.config.CwtDataTypes`
    - 新增 `DefinitionSnippet` `LocalisationSnippet`，位于 `Template` 之后
  - [x] `icu.windea.pls.config.CwtDataTypeSets`
    - 新增 `Snippet`
- [x] 实现相关的 lightElement
  - [x] `icu.windea.pls.lang.psi.ParadoxSnippetElement` - PSI 接口
  - [x] `icu.windea.pls.lang.psi.light.ParadoxDefinitionSnippetLightElement` -  lightElement 实现
  - [x] `icu.windea.pls.lang.psi.light.ParadoxLocalisationSnippetLightElement` -  lightElement 实现
- [x] 实现相关的 dataExpressionSupport
  - [x] `icu.windea.pls.ep.config.configExpression.CwtSnippetDataExpressionSupport` - 新增
  - [x] `CwtDataExpressionMetadata` `CwtDataExpressionMetadataBase` - 新增元数据 `snippetTemplates`
  - [x] `CwtConfigManager.getFullNamesFromSnippet` - 将片段文本解析为一组完整的引用文本
- [x] 实现相关的 scriptExpressionMatcher/csvExpressionMatcher
  - [x] `icu.windea.pls.ep.match.expression.ParadoxCoreScriptExpressionMatcher`
    - `ForDefinitionSnippet` `ForLocalisationSnippet`
  - [x] `icu.windea.pls.ep.match.expression.ParadoxCoreCsvExpressionMatcher`
    - `ForDefinitionSnippet`
  - [x] `icu.windea.pls.lang.match.util.ParadoxMatchResultFactory`
    - `forDefinitionSnippet` `forLocalisationSnippet`
      - 宽松策略：仅要求至少一个对应的引用存在
- [x] 实现相关的 scriptExpressionSupport/csvExpressionSupport
  - [x] `icu.windea.pls.ep.resolve.expression.ParadoxSnippetScriptExpressionSupport`
    - `ForDefinitionSnippet` `ForLocalisationSnippet`
  - [x] `icu.windea.pls.ep.resolve.expression.ParadoxSnippetCsvExpressionSupport`
    - `ForDefinitionSnippet`
- [x] 更新快速文档的相关逻辑
  - [x] `icu.windea.pls.lang.codeInsight.documentation.ParadoxDocumentationManager`
    - `buildForDefinitionSnippet` `buildForLocalisationSnippet`
- [x] 更新代码补全的相关逻辑
  - [x] `icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager`
    - `completeDefinitionSnippet` `completeLocalisationSnippet`
  - [x] `icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionFactory`
    - `fromDefinitionSnippet` `fromLocalisationSnippet`
- [x] lightElement 的关联更改
  - [x] `icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager`
  - [x] `icu.windea.pls.lang.codeInsight.documentation.ParadoxDocumentationManager`
  - [x] `icu.windea.pls.lang.psi.ParadoxElementDescriptionService`
  - [x] `icu.windea.pls.lang.resolve.util.ParadoxExpressionSupportFactory`
- [x] 更新相关测试
  - [x] `icu.windea.pls.core.util.RangeInfoTest` - 补充 `create` `from` 缓存用例
  - [x] `icu.windea.pls.core.util.TemplateInfoTest` - 新增
  - [x] `icu.windea.pls.config.configExpression.CwtDataExpressionTest` - 补充 `testDefinitionSnippet` `testLocalisationSnippet`
- [x] 补充回归测试
  - [x] `icu.windea.pls.test.features.SnippetMatchTest`
    - 覆盖 semanticAnnotator/referenceResolution/completion
    - 测试数据：`src/test/testData/features/snippet/`

任务（step 2）：
- [x] 更新代码检查的相关逻辑
  - [x] `UnresolvedExpressionInspection`
    - 不需要更改相关代码（未匹配时仍按原有逻辑报告）
  - [x] `IncorrectExpressionInspection`
    - 在 `ParadoxIncorrectExpressionCheckers.ForSnippets.kt` 中提供：`ParadoxDefinitionSnippetChecker` `ParadoxLocalisationSnippetChecker`
- [x] 更新自动重命名的相关逻辑（基础支持）
  - [x] `icu.windea.pls.lang.refactoring.rename.naming.ParadoxDefinitionSnippetAutomaticRenamerFactory` - 自动重命名关联的定义
  - [x] `icu.windea.pls.lang.refactoring.rename.naming.ParadoxLocalisationSnippetAutomaticRenamerFactory` - 自动重命名关联的本地化
- [x] 更新相关测试
  - [x] `icu.windea.pls.lang.refactoring.rename.ParadoxDefinitionSnippetRenameTest`
  - [x] `icu.windea.pls.lang.refactoring.rename.ParadoxLocalisationSnippetRenameTest`
  - [x] `UnresolvedExpressionInspectionTest`
  - [x] `IncorrectExpressionInspectionTest`
- [x] 补充回归测试
  - [x] `icu.windea.pls.test.features.SnippetMatchTest`
    - 覆盖 inspection/rename + 包括 unresolvedExpressionInspection/incorrectExpressionInspection
    - 测试数据：`src/test/testData/features/snippet/`

任务（step 3）：
- [x] 对 `icu.windea.pls.lang.codeInsight.navigation` 目录下的所有 action 进行重构（拆分 `isVisible(file)` `isEnabled(file, editor)`）
- [x] 提供对应的导航动作（相关定义/相关本地化）
  - [x] `icu.windea.pls.lang.codeInsight.navigation.GotoRelatedDefinitionsAction`
    - 支持：普通本地化（相关定义）、定义引用片段（相关定义，来自脚本文件）
  - [x] `icu.windea.pls.lang.codeInsight.navigation.GotoRelatedLocalisationsAction`
    - 支持：封装变量/定义/修正（相关本地化）、本地化引用片段（相关本地化，来自脚本文件）
  - [x] `icu.windea.pls.lang.util.ParadoxSnippetManager` - 提供片段的相关定义/相关本地化的解析逻辑
  - [x] `icu.windea.pls.test.features.SnippetMatchTest` - 补充 `*_relatedDefinitions` / `*_relatedLocalisations` 用例

任务（revision）：
- [x] 匹配定义或定义片段时，不忽略定义的子类型。
- [x] `RangeInfo` `TemplateInfo` - 提供方法 `normalize`（使用 `Interner.newWeakInterner()` 进行去重），仅保留静态工厂方法 `create`
- [x] automaticRenamer/automaticRenamerFactory/renameInputValidator 的相关重构 - 提取抽象类，factory 改为内部类
- [x] `RangeInfoTest` `TemplateInfoTest` - 需要使用 expect-dsl
- [x] `CwtDataExpressionTest` - 由于更改了数据表达式的解析策略，需要调整测试用例
- [x] renameTest
  - 出于对齐的目的，统一补充文档注释。
  - 出于对齐的目的，统一使用 `features/refactoring` 作为测试数据目录。
- [x] `SnippetMatchTest`
  - 需要覆盖 semanticAnnotator（可参考已有的回归测试，关键词 `testSemanticAnnotator`）
  - definitionSnippet 与 localisationSnippet 的测试用例放到各自的分组中，并且尽可能地对齐（对于所有已覆盖的场景，两者都应覆盖）
- [x] `UnresolvedExpressionInspectionTest`
  - 尽管 `SnippetMatchTest` 中已覆盖相关的基础场景，这里仍然需要补充，并且应补充更丰富的测试用例，覆盖更多场景（例如正反场景）
- [x] `IncorrectExpressionInspectionTest`
  - 尽管 `SnippetMatchTest` 中已覆盖相关的基础场景，这里仍然需要补充，并且应补充更丰富的测试用例，覆盖更多场景（例如正反场景）

任务（收尾）：
- [x] 补充快照测试
  - [x] `icu.windea.pls.test.chronicle.ChronicleSnapshotTest`
  - [x] `src/test/testData/chronicle/common/guides/`
    - （仅对于脚本文件，不包括 CSV 文件）需要补充、规范化、切分其中的测试数据文件，以及覆盖片段数据类型
- [x] 更新相关文档
  - [x] `ref-config-format.md`
    - [x] （`数据类型` 章节）对片段数据类型的说明
    - [x] （`FAQ` 章节）新增 `关于模板匹配` 章节（修改自 `关于模板表达式` 章节）
    - [x] （`FAQ` 章节）新增 `关于片段匹配` 章节
- [x] 移除与已有的“后缀感知的数据类型/suffixAwareDataTypes”相关的代码和文档

任务（人工）：
- [x] 更新规则文件（包括 Stellaris/VIC3/EU5，包括 rowConfig）

任务（后续修订）：
- [x] definitionSnippet 的快速文档需要加入定义类型信息
- [x] navigationHandler 的后续重构与清理

任务（进阶，待决策）：
- [ ] 考虑将 `|` 改为其他特殊符号字符，以避免类型信息的并集形式中的混淆（如 `<sprite>|$_locked,$_unlocked | ...`）
  - 目前来说，这已经是一个相当合适的选择了（排除 `[...]` `@` `:`）。
- [ ] 考虑对齐片段数据类型的数据表达式（`<entity>|$_a,b_$,c_$_d>`）以及定义展示的位置表达式（`$_desc|$a,$b`）的格式
  - 尽管两者的格式看上去有点相像（管道符边界、逗号分割），但是背后的语义是完全不同的，仍然需要研究如何调整位置表达式的格式，以进行进一步的规范化和对齐。

任务（进阶，暂时搁置）：
- [ ] 考虑进一步完善自动重命名的相关逻辑
  - [ ] `icu.windea.pls.lang.refactoring.rename.naming.ParadoxDefinitionRelatedSnippetsAutomaticRenamer`
    - 这需要首先建立映射关系
  - [ ] `icu.windea.pls.lang.refactoring.rename.naming.ParadoxLocalisationRelatedSnippetsAutomaticRenamer`
    - 这需要首先建立映射关系
- [ ] 考虑提供对应的装订线图标（相关定义/相关本地化）
  - [ ] `icu.windea.pls.lang.codeInsight.markers.script.ParadoxDefinitionSnippetLineMarkerProvider`
    - 由于目前并未为修正（modifier）提供装订线图标（相关项），出于对齐的考虑，暂不提供
  - [ ] `icu.windea.pls.lang.codeInsight.markers.script.ParadoxLocalisationSnippetLineMarkerProvider`
    - 由于目前并未为修正（modifier）提供装订线图标（相关项），出于对齐的考虑，暂不提供

相关代码位置：
- `icu.windea.pls.config.CwtDataTypes.DefinitionSnippet`
- `icu.windea.pls.config.CwtDataTypes.LocalisationSnippet`
- `icu.windea.pls.config.CwtDataTypeSets.Snippet`
- （省略其他）
