# PSI 接口架构与语言抽象调研（3.1.0 快照）

> - 版本：`v3.1.0`
> - 日期：`2026-11-27`
> - 状态：`调研快照`
> - 调研范围：插件四门语言（CWT、Paradox Script、Paradox Localisation、Paradox CSV）的文法层/语义层 PSI 抽象，包括生成 PSI 与手写混入接口、ParserDefinition 与 Lexer、跨语言能力接口、Stub/Light/Injected 边界，以及对应的设计决策与待演进项。

## 摘要

- 四门语言在 PSI 层并不是一套完全统一的模型，而是“共享少量跨语言能力接口 + 各自保留语言特有层级”的结构。
- Paradox Script、Paradox Localisation、Paradox CSV 共享 `ParadoxLanguage`/`ParadoxBaseLanguage` 与 `ParadoxFile`/`ParadoxFileType` 基座，CWT 则直接使用 IntelliJ `Language`/`LanguageFileType`，不进入该基座。
- 文法层以 Grammar-Kit 生成的 `ParadoxScript*`、`ParadoxLocalisation*`、`ParadoxCsv*`、`Cwt*` PSI 接口为主，手写接口集中在 `src/main/kotlin/.../psi` 中，作为混入能力层；语义层主要位于 `icu.windea.pls.lang`，通过扩展属性、Manager、LightElement、引用解析和复杂表达式模型挂接到 PSI。
- Script 与 Localisation 拥有自定义的轻量 Stub 体系（文件级 + 节点级），CSV 与 CWT 当前没有语言 Stub，CSV 的语义主要依赖行规则（`CwtRowConfig`）与文件索引，CWT 的语义则直接转化为规则数据模型 `CwtConfigGroup`。
- Script 与 Localisation 采用双阶段/惰性词法解析：Script 的内联数学经 `IReparseableElementType` 延迟解析，Localisation 的属性值经两级 Lexer 延迟解析；CWT 的选项注释出于性能原因明确不采用惰性解析。
- 跨语言“宏”目前只是概念统一，而不是接口统一：`ParadoxScriptMacro` 与 `ParadoxLocalisationMacro` 分别存在，参数（parameter）也因语法细节差异暂不引入共用 `ParadoxParameter`（见 `documents/notes/adr/macro.md`）。
- 设计文档 `documents/design/language-abstractions.md` 目前只有 Script 的完整块与 Localisation 的占位块，缺少 Localisation 细节、CSV 与 CWT；本报告第 6 节给出可直接回填的结构化材料。

## 调研方法与来源

本次调研优先使用 `intellij-index` MCP 做类型层级与实现关系核验，辅以对源码、BNF/Flex、维护者文档和 GitHub Issue 的直接阅读。

- 代码与文法：`src/main/kotlin/icu/windea/pls/{cwt,script,localisation,csv}/psi/**`、对应 `*.bnf`、`*.flex`、`*ParserDefinition.kt`，以及 `src/main/gen/icu/windea/pls/{cwt,script,localisation,csv}/psi/**` 中生成的 PSI 接口。
- 跨语言 PSI：`src/main/kotlin/icu/windea/pls/lang/psi/**`、`src/main/kotlin/icu/windea/pls/core/psi/**`、`src/main/kotlin/icu/windea/pls/lang/injection/**`。
- 索引与存根：`src/main/kotlin/icu/windea/pls/lang/index/stubs/**`、`src/main/kotlin/icu/windea/pls/lang/index/ChronicleIndexKeys.kt`、`src/main/resources/META-INF/chronicle-lang.xml`。
- 维护者文档：`documents/design/language-abstractions.md`、`documents/notes/guidelines.md`、`documents/notes/adr/` 下的 PSI/语法/Lexer/Macro/Quote 相关 ADR、`documents/notes/ideas.md`、`documents/notes/proposals/minor-syntax-enhancement.md`、`documents/notes/proposals/data-api.md`。
- 参考文档：`docs/zh/ref-syntax.md`、`docs/en/ref-syntax.md`。
- Issue：通过 `gh issue list` / `gh issue view` 检索 `DragonKnightOfBreeze/Paradox-Language-Support` 仓库（当前远端仍使用该仓库名），状态以 2026-11-27 查询结果为准。

需要说明的是，GitHub 上未检索到专门跟踪“PSI 接口重构”的单一 Issue；该主题主要由 ADR 记录，Issue 更多体现为文法、匹配、Stub 索引与具体语言构造的上层缺陷或增强请求。

## 总体 PSI 分层模型

四门语言在实现上都遵循“Lexer → Parser/AST → 生成 PSI → 手写混入能力 → 语义层”的分层，但每层的具体形态不同。

```text
+--------------------------------------------------------------+
| 语义层 (icu.windea.pls.lang / config)                         |
| definition / expression / complexExpression / macro /        |
| references / light elements / stubs / config groups          |
+--------------------------------------------------------------+
| 手写 PSI 混入接口 (src/main/kotlin/**/psi)                    |
| Member / Statement / RichText / Macro / Interpolation /      |
| QuoteAware / Bound / Container / ArgumentAware ...           |
+--------------------------------------------------------------+
| 生成 PSI 接口 (src/main/gen/**/psi)                           |
| ParadoxScript* / ParadoxLocalisation* / ParadoxCsv* / Cwt*   |
+--------------------------------------------------------------+
| ParserDefinition + Parser + Lexer                            |
| Grammar-Kit parser / JFlex lexer / LayeredLexer /            |
| IReparseableElementType / FileType / Language                |
+--------------------------------------------------------------+
```

- 文法层由 `*.bnf` 与 `*.flex` 定义，生成代码位于 `src/main/gen`；手写混入接口与实现工具位于 `src/main/kotlin` 对应语言的 `psi` 包中。
- 生成 PSI 的根接口通常继承 `com.intellij.extapi.psi.ASTWrapperPsiElement`（通过 BNF 的 `extends` 配置），文件实现则继承 `PsiFileBase`。
- 语义层不追求把所有语义都做成 PSI 子类型：定义信息、复杂表达式、规则数据、参数上下文等大量语义模型是通过扩展属性（如 `definitionInfo`）、Manager/Service、引用解析和 LightElement 挂接的。
- 模块边界遵循 `documents/notes/guidelines.md` 的约定：`cwt`、`script`、`localisation`、`csv` 是语义无关的语言基础设施，`lang` 是语义模块；跨语言且领域特定的接口放在 `lang/psi` 或 `core/psi`，但这会带来少量语言模块对 `lang` 的依赖。

### 词法层与解析边界

| 语言 | 词法层结构 | 上下文机制 | 惰性解析/分层 | 备注 |
| --- | --- | --- | --- | --- |
| Script | `ParadoxScript.flex` + `ParadoxScript.InlineMath.flex` | `stateStack` + `expectStack`，用于属性键/字符串/封装变量/参数/条件化块的回退 | `INLINE_MATH_TOKEN` 经 `ParadoxScriptInlineMathElementType` 延迟解析；高亮时叠加内联数学与字符串字面量 Lexer | 见 `adr/script-lexer-refactor.md`、`adr/lexer-reset.md` |
| Localisation | `ParadoxLocalisation.flex` 负责文件结构，`ParadoxLocalisation.Text.flex` 负责富文本 | 文本 Lexer 使用 `stateStack` + `expectStack`，并用 `ParadoxSyntaxConstraint` 检查游戏类型支持 | `PROPERTY_VALUE_TOKEN` 经 `ParadoxLocalisationPropertyValueElementType` 延迟解析；纯文本有快速路径 | 见 `adr/localisation-lexer-refactor.md` |
| CSV | `ParadoxCsv.flex` 单层 | `EOL` 词元 + `checkEol`/`checkColumnToken` | 无惰性解析；高亮时叠加字符串字面量 Lexer | `adr/csv-and-row-config-support.md` |
| CWT | `Cwt.flex` 单层，带选项注释与嵌套选项状态 | `IN_OPTION`/`IN_OPTION_VALUE`/`IN_OPTION_NESTED` 状态 | 选项注释明确不惰性解析（性能原因）；高亮时叠加字符串字面量 Lexer | `Cwt.bnf` 中的 `2.0.7` 注释 |

## 四门语言的 PSI 谱系

### Paradox Script

文件与成员结构：

```text
PsiFile
└─ ParadoxScriptFile
   implements ParadoxFile, ParadoxScriptMemberContext, ParadoxScriptMember,
              ParadoxDefinitionElement
   └─ ParadoxScriptRootBlock
      implements ParadoxScriptMemberContainer, PsiRootBlock, PsiListLikeElement
      ├─ ParadoxScriptStatement
      │  ├─ ParadoxScriptMember
      │  │  ├─ ParadoxScriptProperty
      │  │  └─ ParadoxScriptValue
      │  ├─ ParadoxScriptScriptedVariable
      │  └─ ParadoxScriptNormalConditionalBlock
      └─ (comment / 空白等)
```

值与其他表达式节点：

```text
ParadoxScriptExpressionElement
├─ ParadoxScriptPropertyKey
│  implements ParadoxScriptLiteralValue, ParadoxScriptStringExpressionElement,
│             ParadoxScriptInterpolationContainer, PsiQuoteAwareElement
└─ ParadoxScriptValue
   implements ParadoxScriptMember
   ├─ ParadoxScriptBoolean
   ├─ ParadoxScriptNumber
   │  ├─ ParadoxScriptInt
   │  └─ ParadoxScriptFloat
   ├─ ParadoxScriptString
   │  implements ParadoxScriptLiteralValue, ParadoxScriptStringExpressionElement,
   │             ParadoxScriptInterpolationContainer, ParadoxLanguageInjectionHost,
   │             PsiQuoteAwareElement
   ├─ ParadoxScriptBlock
   ├─ ParadoxScriptColor
   ├─ ParadoxScriptScriptedVariableReference
   └─ ParadoxScriptInlineMath
```

语法宏与插值相关节点：

```text
ParadoxScriptMacro
├─ ParadoxScriptInterpolation
│  ├─ ParadoxScriptParameter
│  │  ├─ ParadoxScriptNormalParameter
│  │  └─ ParadoxScriptInlineMathParameter
│  └─ ParadoxScriptInlineConditionalBlock
├─ ParadoxScriptScriptedVariableReference
├─ ParadoxScriptInlineMathScriptedVariableReference
├─ ParadoxScriptInlineMath
└─ ParadoxScriptConditionalBlock
   ├─ ParadoxScriptNormalConditionalBlock
   └─ ParadoxScriptInlineConditionalBlock

ParadoxScriptArgument
├─ ParadoxScriptNormalParameterArgument
│  implements ParadoxLanguageInjectionHost
└─ ParadoxScriptInlineMathParameterArgument
```

- 关键接口位于 `src/main/kotlin/icu/windea/pls/script/psi/`，例如 `ParadoxScriptMember.kt:12`、`ParadoxScriptMemberContext.kt:12`、`ParadoxScriptMemberContainer.kt:10`、`ParadoxScriptStatement.kt:13`、`ParadoxScriptMacro.kt:18`、`ParadoxScriptInterpolation.kt:14`、`ParadoxScriptParameter.kt:18`、`ParadoxScriptConditionalBlock.kt:11`。
- 生成接口位于 `src/main/gen/icu/windea/pls/script/psi/`；`ParadoxScriptProperty` 与 `ParadoxScriptScriptedVariable` 另外实现 `StubBasedPsiElement`。
- `ParadoxScriptFile` 同时是成员、成员上下文和定义元素，这是 Script 相对于其他语言的一个显著特例，见 `src/main/kotlin/icu/windea/pls/script/psi/ParadoxScriptFile.kt:10`。
- 内联数学的根节点 `ParadoxScriptInlineMathRoot` 是 BNF 的 `extraRoot`，从 `INLINE_MATH_TOKEN` 延迟解析得到；其表达式节点包括二元、一元、分组、绝对值、因子、数字、封装变量引用与参数。

### Paradox Localisation

文件与属性结构：

```text
PsiFile
└─ ParadoxLocalisationFile
   implements ParadoxFile
   └─ ParadoxLocalisationPropertyList
      implements PsiListLikeElement, StubBasedPsiElement<ParadoxLocalisationPropertyListStub>
      ├─ ParadoxLocalisationLocale
      │  implements NavigatablePsiElement, StubBasedPsiElement<ParadoxLocalisationLocaleStub>
      └─ ParadoxLocalisationProperty
         implements ParadoxLocalisationNamedElement, PsiPresentableTextAwareElement,
                    NavigatablePsiElement, StubBasedPsiElement<ParadoxLocalisationPropertyStub>
         ├─ ParadoxLocalisationPropertyKey
         └─ ParadoxLocalisationPropertyValue
            implements ParadoxLocalisationRichTextContainer, PsiQuoteAwareElement
```

富文本与插值结构：

```text
ParadoxLocalisationRichText
├─ ParadoxLocalisationText
├─ ParadoxLocalisationColorfulText
│  implements ParadoxLocalisationTextColorAwareElement,
│             ParadoxLocalisationRichTextContainer
├─ ParadoxLocalisationParameter
│  implements ParadoxLocalisationInterpolation,
│             ParadoxLocalisationInterpolationContainer,
│             ParadoxLocalisationArgumentAwareElement
├─ ParadoxLocalisationCommand
│  implements ParadoxLocalisationInterpolation,
│             ParadoxLocalisationArgumentAwareElement
├─ ParadoxLocalisationConceptCommand
├─ ParadoxLocalisationIcon
│  implements ParadoxLocalisationInterpolationContainer,
│             ParadoxLocalisationArgumentAwareElement
├─ ParadoxLocalisationTextIcon
│  implements ParadoxLocalisationInterpolationContainer
└─ ParadoxLocalisationTextFormat
   implements ParadoxLocalisationInterpolationContainer
```

语法宏与文法附加结构：

```text
ParadoxLocalisationMacro
├─ ParadoxLocalisationInterpolation
│  ├─ ParadoxLocalisationParameter
│  └─ ParadoxLocalisationCommand
├─ ParadoxLocalisationScriptedVariableReference
└─ ParadoxLocalisationTaggedParameter

ParadoxLocalisationRichTextContainer
├─ ParadoxLocalisationPropertyValue
├─ ParadoxLocalisationColorfulText
├─ ParadoxLocalisationConceptString
├─ ParadoxLocalisationTextFormatString
├─ ParadoxLocalisationStringVariantString
└─ ParadoxLocalisationTagSensitiveTextString

ParadoxLocalisationArgument
├─ ParadoxLocalisationParameterArgument
├─ ParadoxLocalisationCommandArgument
└─ ParadoxLocalisationIconArgument

ParadoxLocalisationExpressionElement
├─ ParadoxLocalisationCommandText
│  implements ParadoxLocalisationInterpolationContainer
└─ ParadoxLocalisationConceptName
   implements ParadoxLocalisationInterpolationContainer

文法附加节点
├─ ParadoxLocalisationTag / ContextTag
├─ ParadoxLocalisationTagPart / ContextTagPart
├─ ParadoxLocalisationStringVariantSet / StringVariant / StringVariantTagPart
└─ ParadoxLocalisationTagSensitiveText / TaggedParameter
```

- 关键接口位于 `src/main/kotlin/icu/windea/pls/localisation/psi/`，例如 `ParadoxLocalisationRichTextContainer.kt:13`、`ParadoxLocalisationInterpolation.kt:14`、`ParadoxLocalisationInterpolationContainer.kt:25`、`ParadoxLocalisationMacro.kt:19`。
- 生成接口位于 `src/main/gen/icu/windea/pls/localisation/psi/`；`ParadoxLocalisationPropertyList`、`ParadoxLocalisationLocale`、`ParadoxLocalisationProperty` 实现 `StubBasedPsiElement`。
- 与 Script/CWT 不同，Localisation 没有统一的 `Member`/`Statement` 接口；它采用“属性列表 + 富文本容器”的扁平模型，富文本节点直接承担语法角色。
- 属性值节点不是由顶层 Lexer 一次性完整切分，而是通过 `ParadoxLocalisationPropertyValueElementType` 从 `PROPERTY_VALUE_TOKEN` 延迟解析为 `TEXT_ROOT`，详见 `src/main/kotlin/icu/windea/pls/localisation/psi/ParadoxLocalisationPropertyValueElementType.kt`。

### Paradox CSV

列与行结构：

```text
PsiFile
└─ ParadoxCsvFile
   implements ParadoxFile
   ├─ ParadoxCsvHeader
   │  implements ParadoxCsvColumnContainer, NavigatablePsiElement, PsiListLikeElement
   └─ ParadoxCsvRow
      implements ParadoxCsvColumnContainer, NavigatablePsiElement, PsiListLikeElement
      └─ ParadoxCsvColumn
         implements ParadoxCsvExpressionElement, ParadoxCsvLiteralValue,
                    PsiQuoteAwareElement, PsiPresentableTextAwareElement,
                    NavigatablePsiElement
```

- CSV 的生成 PSI 极少：只有 `ParadoxCsvHeader`、`ParadoxCsvRow`、`ParadoxCsvColumn` 三个节点接口；文件接口 `ParadoxCsvFile` 在手写代码中提供 `header` 与 `rows` 访问器。
- CSV 没有成员、语句、宏、参数或插值层级；列是唯一的表达式/字面量载体。
- `ParadoxCsvColumn` 同时实现 `ParadoxCsvExpressionElement`（进而实现跨语言 `ParadoxExpressionElement`）与 `ParadoxCsvLiteralValue`（进而实现平台 `PsiLiteralValue`），因此可以参与统一的表达式解析与引用解析。
- 文件层不实现 `StubBasedPsiElement`，也没有节点存根；列与表头的关系通过 `ParadoxCsvPsiService` 动态计算，见 `src/main/kotlin/icu/windea/pls/csv/psi/ParadoxCsvPsiService.kt`。

### CWT

成员与规则结构：

```text
PsiFile
└─ CwtFile
   implements CwtMemberContext
   └─ CwtRootBlock
      implements CwtMemberContainer, PsiRootBlock, PsiListLikeElement
      ├─ CwtMember
      │  ├─ CwtProperty
      │  │  implements CwtNamedElement, PsiPresentableTextAwareElement,
      │  │             NavigatablePsiElement
      │  └─ CwtValue
      │     implements CwtExpressionElement, CwtOptionMember,
      │                PsiPresentableTextAwareElement, NavigatablePsiElement
      ├─ CwtOptionComment
      │  implements PsiComment
      │  ├─ CwtOption
      │  │  implements CwtNamedElement, CwtOptionMember
      │  └─ CwtValue（作为 option value）
      └─ CwtDocComment
         implements PsiDocCommentBase
```

CWT 的值与表达式：

```text
CwtExpressionElement
├─ CwtPropertyKey
│  implements CwtStringExpressionElement, CwtLiteralValue, PsiQuoteAwareElement
└─ CwtValue
   ├─ CwtBoolean
   ├─ CwtNumber
   │  ├─ CwtInt
   │  └─ CwtFloat
   ├─ CwtString
   │  implements CwtNamedElement, CwtStringExpressionElement,
   │             CwtLiteralValue, PsiQuoteAwareElement
   └─ CwtBlock
      implements CwtMemberContainer, CwtBoundMemberContainer, PsiListLikeElement

CwtOptionMember
├─ CwtOption
└─ CwtValue
```

- CWT 的成员模型与 Script 最接近，但接口完全独立：`CwtMember`/`CwtMemberContext`/`CwtMemberContainer`/`CwtStatement` 与 `ParadoxScript*` 同名概念不共享父接口。
- `CwtValue` 同时是 `CwtMember` 与 `CwtOptionMember`，因此值既可以作为普通成员出现在块中，也可以作为选项注释中的选项值。
- `CwtExpressionElement` 不继承跨语言 `ParadoxExpressionElement`；CWT 是规则语言，其表达式用于规则解析，而不是游戏脚本表达式。
- CWT 没有宏、参数、插值或封装变量；但它拥有三级注释系统（普通注释、选项注释、文档注释），并明确不将选项注释抽成惰性解析节点（见 `Cwt.bnf` 中 `2.0.7 DO NOT use lazy-parsing due to performance reasons` 的注释）。

### 谱系对照表

| 维度 | Script | Localisation | CSV | CWT |
| --- | --- | --- | --- | --- |
| 语言基类 | `ParadoxLanguage` → `ParadoxBaseLanguage` | `ParadoxLanguage` → `ParadoxBaseLanguage` | `ParadoxLanguage` → `ParadoxBaseLanguage` | 直接继承 `Language` |
| 文件基接口 | `ParadoxFile` + `ParadoxFileType` | `ParadoxFile` + `ParadoxFileType` | `ParadoxFile` + `ParadoxFileType` | `PsiFile` + `LanguageFileType` |
| 成员/语句模型 | `Member` / `Statement` / `MemberContainer` | 无，直接使用属性与富文本容器 | 无，直接使用 Header/Row/Column | `CwtMember` / `CwtStatement` / `CwtMemberContainer` |
| 表达式接口 | `ParadoxScriptExpressionElement` | `ParadoxLocalisationExpressionElement` | `ParadoxCsvExpressionElement` | `CwtExpressionElement` |
| 跨语言表达式 | 是，`ParadoxExpressionElement` | 是，`ParadoxExpressionElement` | 是，`ParadoxExpressionElement` | 否 |
| 宏/插值 | `ParadoxScriptMacro` / `Interpolation` | `ParadoxLocalisationMacro` / `Interpolation` | 无 | 无 |
| 文件级存根 | `ParadoxScriptFileStub` | `ParadoxLocalisationFileStub` | 无 | 无 |
| 节点级存根 | Property、ScriptedVariable | PropertyList、Locale、Property | 无 | 无 |
| LightElement | 游戏语义相关 LightElement | 游戏语义相关 LightElement | 复杂枚举值等 LightElement | 规则符号 LightElement |
| 惰性解析 | 内联数学、字符串插值相关词元 | 属性值富文本 | 无 | 选项注释明确不惰性解析 |
| 文件类型扩展名 | `.txt` 等，按路径重载 | `.yml` | `.csv` | `.cwt` |

## 跨语言公共基接口与能力接口

### 语言与文件基座

- `icu.windea.pls.lang.ParadoxLanguage` 是抽象语言基类，`ParadoxBaseLanguage` 是 `PARADOX` 基语言；Script、Localisation、CSV 三者的 `getBaseLanguage()` 都指向它。
- `icu.windea.pls.lang.psi.ParadoxFile` 只要求 `getFileType(): ParadoxFileType`；`ParadoxFileType` 是 `LanguageFileType` 的抽象子类。
- CWT 不在该基座内：`CwtLanguage` 直接继承 `com.intellij.lang.Language`，`CwtFileType` 直接继承 `LanguageFileType`，`CwtFile` 只实现 `PsiFile` 与 `CwtMemberContext`。
- `ParadoxFileTypeOverrider` 依据入口目录与路径重载文件类型，因此 `.gfx`、`.gui`、`.txt` 等扩展名可以按语义路径映射到 `PARADOX_SCRIPT`；注入文件（`VirtualFileWindow`）也经过该重载器。

这一划分的事实依据是 `src/main/kotlin/icu/windea/pls/lang/ParadoxLanguage.kt:5`、`ParadoxBaseLanguage.kt:3`、`ParadoxFile.kt:6`、`ParadoxFileType.kt:6` 以及各语言的 `*Language.kt`、`*FileType.kt`。

### 通用 PSI 能力接口

| 接口 | 位置 | 作用 | 主要实现者 |
| --- | --- | --- | --- |
| `PsiQuoteAwareElement` | `core/psi/PsiQuoteAwareElement.kt:17` | 暴露 `quotePattern`，只提供策略，不直接提供修改方法 | Script 的 PropertyKey/String；CWT 的 OptionKey/PropertyKey/String；Localisation 的 PropertyValue；CSV 的 Column |
| `PsiBoundElement` | `core/psi/PsiBoundElement.kt:8` | 可带左右边界的元素，边界可为空 | Script Block/InlineMath/ConditionalBlock；CWT Block |
| `PsiRootBlock` | `core/psi/PsiRootBlock.kt:10` | 文件根的“无边界的块” | Script RootBlock；CWT RootBlock |
| `PsiPresentableTextAwareElement` | `core/psi/PsiPresentableTextAwareElement.kt:13` | 提供区别于原始文本的展示文本 | 四门语言的大量节点 |
| `PsiReadWriteAccessAwareElement` | `core/psi/PsiReadWriteAccessAwareElement.kt:6` | 提供读写访问语义，供 LightElement 使用 | CWT 符号 LightElement、动态值/参数/修饰符等 LightElement |
| `ParadoxExpressionElement` | `lang/psi/ParadoxExpressionElement.kt:24` | 跨语言表达式基接口，提供 name/value/setValue/setContent | Script、Localisation、CSV 的表达式节点；CWT 不实现 |
| `ParadoxDefinitionElement` | `lang/psi/ParadoxDefinitionElement.kt:23` | 可承载定义语义的元素，带 name/nameIdentifier/block | Script Property、Script File；Localisation/CSV/CWT 不实现 |
| `ParadoxScriptedVariableReference` | `lang/psi/ParadoxScriptedVariableReference.kt:10` | 跨语言封装变量引用能力 | Script 的引用/内联数学引用；Localisation 的 `$@var$` 引用 |
| `ParadoxSnippetElement` | `lang/psi/ParadoxSnippetElement.kt:17` | 表示去除前后缀后的“片段引用” | Definition/Localisation 的 Snippet LightElement |
| `ParadoxExternalReferenceElement` | `lang/psi/ParadoxExternalReferenceElement.kt:12` | 表示声明位于外部文件中的引用 | ShaderEffect/MeshLocator LightElement |
| `ParadoxLanguageInjectionHost` | `lang/psi/ParadoxLanguageInjectionHost.kt:9` | 语言注入宿主能力 | Script 的 String 与 NormalParameterArgument |
| `ParadoxStub` / `ParadoxLocaleAwareStub` | `lang/psi/stubs/` | 带 gameType（以及 locale）的存根基接口 | Script/Localisation 的存根 |

### 引号处理与 `QuotePattern`

- `PsiQuoteAwareElement` 只暴露 `quotePattern`，具体的 `needQuote`/`canQuote`/`canUnquote`/`quoteIfNeeded` 等逻辑统一由 `icu.windea.pls.core.text.QuotePattern` 及其 `Base` 实现承担，扩展函数位于 `core/TextExtensions.kt`。
- 各语言提供自己的 `QuotePattern` 扩展：`QuotePatterns.ParadoxScript` 在 `script/text/ParadoxScriptQuotePattern.kt`，对边界字符有额外限制；`QuotePatterns.ParadoxLocalisation` 在 `localisation/text/ParadoxLocalisationQuotePattern.kt`，基本不强制加引号；`QuotePatterns.Cwt` 与 `QuotePatterns.ParadoxCsv` 分别在 `cwt/text` 与 `csv/text` 下。
- 这套设计是 `documents/notes/adr/quote-pattern.md` 的实施结果：将引号相关元数据与断言/修改逻辑收敛为策略对象，避免各语言重复实现。

### 宏、参数与插值的跨语言边界

- `ParadoxScriptMacro` 与 `ParadoxLocalisationMacro` 是概念对应但彼此独立的接口：它们都表示“语法层面可确认的宏”，但类型系统上没有共同父接口。
- `ParadoxScriptInterpolation` 与 `ParadoxLocalisationInterpolation` 分别表示“内联形式的语法宏”，并各自定义可承载插值的容器。
- 参数没有共用接口：`ParadoxScriptParameter` 只有 Script 版本，Localisation 使用 `ParadoxLocalisationParameter`；这是 `documents/notes/adr/macro.md` 的明确决策，原因是两种语言的参数语法细节和变体不同。
- Localisation 的 `ParadoxLocalisationTaggedParameter`（`<PARAM>`）当前只实现 `ParadoxLocalisationMacro`，尚未并入统一的参数接口；这与 `documents/notes/adr/localisation-syntax-grammatical.md` 中“计划将 `ParadoxLocalisationParameter` 拆为 Normal/Tagged”的待办一致。
- `ParadoxScriptMacro` 的实现包括封装变量引用、内联数学、条件化块与参数；`ParadoxLocalisationMacro` 的实现包括参数、命令、封装变量引用与 tagged parameter。

### Stub、Light 与 Injected 边界

#### Stub

- 全局存根基接口是 `ParadoxStub<T>`（要求 `gameType`，父存根可转回 `ParadoxStub`）与 `ParadoxLocaleAwareStub<T>`（增加 `locale`）。
- Script 的存根包括 `ParadoxScriptFileStub`、`ParadoxScriptPropertyStub`（含 Define/DefineNamespace/DefineVariable/InlineScriptUsage/InlineScriptArgument 等 sealed 变体）、`ParadoxScriptScriptedVariableStub`。
- Localisation 的存根包括 `ParadoxLocalisationFileStub`、`ParadoxLocalisationPropertyListStub`、`ParadoxLocalisationLocaleStub`、`ParadoxLocalisationPropertyStub`。
- 存根的构建入口是自定义 `LightLanguageStubDefinition`：`ParadoxScriptStubDefinition` 与 `ParadoxLocalisationStubDefinition`，它们提供 `stubVersion`、`shouldBuildStubFor`、`parseContentsLight`。
- 节点存根通过 `StubRegistryExtension` 注册：`ParadoxScriptStubRegistry` 为 `SCRIPTED_VARIABLE`/`PROPERTY` 注册 `LightStubElementFactory` 与序列化器；`ParadoxLocalisationStubRegistry` 为 `PROPERTY_LIST`/`LOCALE`/`PROPERTY` 做同样的事。
- 索引侧由 `ChronicleIndexKeys` 与 `stubIndex` 注册驱动，包括脚本封装变量名、定义命名空间/变量、内联脚本用法/参数，以及本地化名、同步本地化名、受限本地化名等。
- 值得注意的快照事实：`ParadoxScriptElementTypeFactory` 的 `SCRIPTED_VARIABLE`/`PROPERTY` 与 `ParadoxLocalisationElementTypeFactory` 的 `PROPERTY` 目前都映射为普通 `IElementType`，原先映射到专用 StubElementType 的分支已被注释；因此普通 AST 解析不依赖平台 AST stub 树，跨文件索引走自定义 light stub。PSI 接口仍声明为 `StubBasedPsiElement`，这是后续需要明确的“双模式”边界。
- CSV 与 CWT 没有语言存根；CSV 的语义依赖 `CwtRowConfig`、文件索引与引用解析，CWT 的语义依赖规则数据模型与 `CwtConfigSymbolIndex` 等文件索引。

#### LightElement

- 基础类是 `core/psi/light/LightElementBase`：包装父 PSI，继承父元素的 language/containingFile/project，非物理元素，默认不提供 textRange/textOffset。
- 游戏语义 LightElement 的基类是 `lang/psi/light/ParadoxLightElementBase`；规则语义 LightElement 的基类是 `lang/psi/light/CwtConfigLightElementBase`。
- 现有 LightElement 主要包括：`ParadoxParameterLightElement`、`ParadoxLocalisationParameterLightElement`、`ParadoxComplexEnumValueLightElement`、`ParadoxDynamicValueLightElement`、`ParadoxModifierLightElement`、`ParadoxShaderEffectLightElement`、`ParadoxMeshLocatorLightElement`、`ParadoxDefinitionSnippetLightElement`、`ParadoxLocalisationSnippetLightElement`、`CwtConfigSymbolLightElement`、`CwtMemberConfigLightElement`。
- LightElement 的定位是“可作为导航目标或相关项的语义节点”，不是完整声明：例如 Snippet LightElement 只保存片段文本与模板参数，参数 LightElement 由参数上下文解析产生，复杂枚举值 LightElement 由索引/规则解析产生。
- `documents/notes/ideas.md:434-436` 提到过内联定义的 `definitionLightElement`，但当前 `lang/psi/light` 下没有对应的定义 LightElement 实现；现有定义语义主要由 `ParadoxDefinitionInfo` 与 Script PSI 承载，这一点需要在后续设计文档中明确。

#### Injected

- 注入宿主是 `ParadoxLanguageInjectionHost`，当前只有 Script 的 `ParadoxScriptString` 与 `ParadoxScriptNormalParameterArgument` 实现它。
- `ParadoxLanguageInjector` 是 `MultiHostInjector`：对参数实参与参数默认值注入 `ParadoxScriptLanguage`，对看起来像富文本的带引号字符串注入 `ParadoxLocalisationLanguage`（注入时使用合成的 `INJECTED: "` 前缀与 `"` 后缀，见 `ParadoxLocalisationTextInjectionInfo`）。
- `ParadoxLanguageInjectionService` 负责计算注入范围与参数元素，`ParadoxLanguageInjectionManager` 负责识别注入文件、回溯顶层宿主、读取注入切片。
- 注入文件边界的关键规则在 `ParadoxPsiFileMatchService.isScriptFile/isLocalisationFile/isCsvFile`：如果文件没有文件信息，则仅当它是来自 Script 的注入文件时才视为有效；这保证了内联脚本参数值、参数默认值等注入 PSI 可以参与语义解析，而不会误把任意无路径文件当作游戏文件。
- 注入文件也会经过 `ParadoxFileTypeOverrider` 的 `VirtualFileWindow` 分支，从而按宿主上下文选择文件类型。

## 文法层与语义层映射

### Paradox Script 对齐点

| 文法层节点 | 语义层模型/能力 | 说明 |
| --- | --- | --- |
| `ParadoxScriptFile` | `ParadoxDefinitionSource.File`、`definitionInfo`、文件信息 | 文件本身可作为定义来源，也可作为成员上下文 |
| `ParadoxScriptProperty` | 定义（`ParadoxDefinitionInfo`）、定值（`ParadoxDefineNamespaceInfo`/`ParadoxDefineVariableInfo`）、内联脚本用法、定义注入用法、普通属性 | 同一个文法节点由规则上下文决定语义角色 |
| `ParadoxScriptScriptedVariable` | `ParadoxScriptedVariableType`（本地/全局）、`ScriptedVariableName` 索引 | 名称可参数化时不进入索引 |
| `ParadoxScriptScriptedVariableReference` | 封装变量引用、`ParadoxScriptMacro`、引用解析 | 可作为独立值，也可嵌入字符串或内联数学 |
| `ParadoxScriptValue` | `ParadoxExpression`、`ParadoxComplexExpression`、颜色/内联数学评估 | 具体语义由匹配到的规则数据类型决定 |
| `ParadoxScriptParameter` | 参数上下文、`ParadoxParameterLightElement`、默认值/实参注入 | 文法上几乎可在任意位置出现，语义上受规则约束 |
| `ParadoxScriptNormalConditionalBlock` | 条件化块、语法宏、成员容器 | 既可展开为语句，也可作为成员容器参与匹配 |
| `ParadoxScriptInlineConditionalBlock` | 插值、语法宏、插值容器 | 内联形式，可与标识符/字面量词元任意组合 |
| `ParadoxScriptInlineMath` | 语法宏、内联数学表达式、求值器 | 从 `INLINE_MATH_TOKEN` 惰性解析，表达式有独立优先级文法 |
| `ParadoxScriptColor` | 颜色类型、颜色参数、`ParadoxColorService` | 当前整体作为 `COLOR_TOKEN`，待拆分为更具体的词元 |
| `ParadoxScriptBlock` | 成员容器、值、边界元素 | 表达式配置可继续下钻 |

Script 的语义层级可以概括为：

- **封装变量**：`ParadoxScriptScriptedVariable` 对应本地/全局封装变量，索引与 LightElement 参数解析共同参与名称解析。
- **定义**：`ParadoxDefinitionElement` + `ParadoxDefinitionInfo`，包含 `rootKeys`、`typeKey`、`name`、`memberPath` 等；定义来源包括文件、属性、内联、注入（`ParadoxDefinitionSource`）。
- **宏**：语法宏对应 `ParadoxScriptMacro` 家族；语义宏对应内联脚本（`ParadoxInlineScriptManager`/`ParadoxInlineScriptService`）与定义注入（`ParadoxDefinitionInjectionManager`），规则侧对应 `CwtMacroConfig`。
- **复杂表达式**：`ParadoxComplexExpression` 从 `ParadoxExpressionElement` 和规则数据类型解析而来，包含作用域字段、值字段、变量字段、命令、动态值、脚本值引用、定值引用、数组定值引用、标签集合、数据库对象、命名格式、模板等子类型（`src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxComplexExpression.kt:40`）。
- **定值与动态值**：`ParadoxDefineNamespaceInfo`/`ParadoxDefineVariableInfo`、`ParadoxDynamicValueLightElement` 等由索引与规则共同产生。

### Paradox Localisation 对齐点

| 文法层节点 | 语义层模型/能力 | 说明 |
| --- | --- | --- |
| `ParadoxLocalisationFile` | 文件信息、`ParadoxLocalisationType`、locale 索引 | 无定义元素接口 |
| `ParadoxLocalisationPropertyList` | locale 分组、存根与索引 | 可对应多个 locale 段，兼容 `languages.yml` |
| `ParadoxLocalisationLocale` | 语言区域、`ParadoxLocaleAwareStub.locale` | 词法层通过启发式区分 locale 与 property key |
| `ParadoxLocalisationProperty` | 本地化条目（name/type/locale）、`LocalisationName` 索引、相关定义/本地化 | `.yml` 中每一行本地化 |
| `ParadoxLocalisationPropertyValue` | 富文本容器、引号策略 | 惰性解析为 `TEXT_ROOT` |
| `ParadoxLocalisationColorfulText` | `ParadoxTextColorManager`、`textColor` 定义、`ParadoxLocalisationTextColorAwareElement` | 颜色标记 `§...§!` |
| `ParadoxLocalisationParameter` | 本地化引用/参数/封装变量引用、`ParadoxLocalisationParameterLightElement`、`ParadoxCommandExpression` | `$...$`，可带 `|ARG` |
| `ParadoxLocalisationCommand` | `ParadoxCommandExpression`、`ParadoxComplexExpression`、`ParadoxLocalisationArgument` | `[...]`，可带 `&TAG`、`::` 等附加文法 |
| `ParadoxLocalisationConceptCommand` | concept 定义、可选 concept string | Stellaris 概念命令 |
| `ParadoxLocalisationIcon` | `ParadoxLocalisationIconSupport` EP、sprite/图片/定义 | `£...£`，可带 `|frame` |
| `ParadoxLocalisationTextIcon` | 文本图标定义、文件索引 | CK3/VIC3/EU5 |
| `ParadoxLocalisationTextFormat` | 文本格式定义、文件索引 | CK3/VIC3/EU5 |
| `ParadoxLocalisationTag` / `ContextTag` | 目前主要是文法节点 | ADR 计划将其作为动态引用处理 |
| `ParadoxLocalisationStringVariantSet` / `StringVariant` | 目前主要是文法节点 | 字符串变体，尚未完全语义化 |
| `ParadoxLocalisationTagSensitiveText` / `TaggedParameter` | 目前主要是文法节点 | `<PARAM>` 尚未并入统一参数接口 |

Localisation 的语义层级可以概括为：

- **本地化条目**：`ParadoxLocalisationProperty` 是名称、类型（Normal/Synced）与 locale 的载体；其名称通过 StubIndex 查询，并与定义、图片、修饰符等相关信息关联。
- **富文本**：`RichText` 节点是语法角色，语义上分别落到颜色定义、图标解析、文本格式/文本图标定义、概念定义、命令表达式和参数引用。
- **参数与命令**：`ParadoxLocalisationParameter` 与 `ParadoxLocalisationCommand` 是 `ParadoxLocalisationInterpolation`，分别对应参数注入/本地化引用与命令表达式；命令文本可由 `ParadoxCommandExpression` 进一步解析为作用域链与 `Get...` 方法。
- **语法宏**：`ParadoxLocalisationMacro` 家族覆盖参数、命令、封装变量引用、tagged parameter；其语义实现仍以各语言的解析器/引用解析器为主，而不是统一宏模型。
- **标签与变体**：`Tag`、`ContextTag`、`StringVariantSet`、`TagSensitiveText` 目前主要停留在文法层；`localisation-syntax-grammatical.md` 明确计划补上动态引用、标签数量检查、tagged parameter 统一等语义能力。

### Paradox CSV 对齐点

| 文法层节点 | 语义层模型/能力 | 说明 |
| --- | --- | --- |
| `ParadoxCsvFile` | 文件信息、行规则匹配 | 通过路径与扩展名匹配 `CwtRowConfig` |
| `ParadoxCsvHeader` | 列名列表、`CwtRowConfig.columns`、列索引 | 表头用于列名与列索引匹配；在 CSV 中声明定义尚未实施 |
| `ParadoxCsvRow` | 行数据、列数检查、`skip_last_row`/`skip_last_column` | 行规则可声明按 key 或 index 匹配 |
| `ParadoxCsvColumn` | `ParadoxExpressionElement`、`ParadoxComplexExpression`、复杂枚举值 | 列可声明 `## declare_complex_enum` |
| `ParadoxCsvPsiService` | 列名、列索引、表头列、空列位置 | 语义层辅助服务，不是 PSI 接口 |
| `ParadoxCsvExpressionPsiReferenceProvider` | 表达式引用解析 | 列值可引用定义/复杂枚举等 |
| `ParadoxComplexEnumValueLightElement` | 复杂枚举值作为相关项/引用目标 | 来自 CSV 列规则与脚本表达式两侧 |

CSV 的语义层级可以概括为：

- **表结构**：`Header` 提供列名/列索引；`Row` 是数据记录；`Column` 是唯一的表达式/字面量节点。
- **行规则**：`CwtRowConfig` 定义路径匹配、`type`（key/index）、`columns`、`skip_last_row`、`skip_last_column`，并可通过列规则上的 `## declare_complex_enum` 声明复杂枚举值（`src/main/kotlin/icu/windea/pls/config/config/delegated/CwtRowConfig.kt:62`）。
- **表达式**：CSV 列通过 `ParadoxCsvExpressionElement` 接入跨语言表达式解析，因此可以使用 `ParadoxComplexExpression` 的通用能力。
- **索引**：CSV 没有语言存根；复杂枚举值、定义引用、路径等由文件索引与配置匹配共同驱动，若未来需要更重的跨文件名称解析，需要重新评估是否引入 Stub。

### CWT 对齐点

| 文法层节点 | 语义层模型/能力 | 说明 |
| --- | --- | --- |
| `CwtFile` | 参与 `CwtConfigGroup` 构建（由 `CwtConfigGroupProcessor`/`CwtConfigGroupFileProvider` 驱动） | CWT 是规则源，不是游戏数据 |
| `CwtRootBlock` / `CwtBlock` | 规则成员容器、嵌套配置 | 成员的父子关系对应规则层级 |
| `CwtProperty` | `CwtPropertyConfig` | 属性规则，可携带键表达式、值表达式、分隔符类型与子规则 |
| `CwtValue` | `CwtValueConfig` | 值规则，可作为属性值或直接值 |
| `CwtOption` / `CwtOptionComment` | `CwtOptionMetadata`（由 `CwtOptionMetadataService` 处理） | 选项注释映射为基数、严重级别、作用域等元数据 |
| `CwtDocComment` | 文档文本、Markdown 文档 | `####` 及以上按 Markdown 处理 |
| `CwtConfigSymbolLightElement` / `CwtMemberConfigLightElement` | 规则符号、成员规则的 LightElement 表示 | 用于导航/展示规则符号 |

CWT 的语义层级可以概括为：

- **规则模型**：CWT PSI 经解析后转化为 `CwtConfig` 家族（`CwtPropertyConfig`、`CwtValueConfig`、`CwtOptionConfig`、`CwtMemberConfig` 等），再汇总为 `CwtConfigGroupDataModel`。
- **规则分组**：`CwtConfigGroup` 暴露 `types`、`rows`、`defineNamespaces`、`enums`、`complexEnums`、`unions`、`dynamicValueTypes`、`links`、`localisationCommands`、`localisationPromotions`、`scopes`、`scopeGroups`、`modifiers`、`modifierCategories`、`databaseObjectTypes`、`aliasGroups`、`singleAliases`、`macros` 等语义数据。
- **选项元数据**：`CwtOptionMetadata` 使 `## cardinality`、`## severity`、`## push_scope` 等选项注释成为规则行为的一部分，而不是普通字符串。
- **文档注释**：`CwtDocComment` 既是 `PsiDocCommentBase`，又参与快速文档与补全展示；`####` 前缀触发的 Markdown 处理是 CWT 注释系统的特殊约定。

### 对 `language-abstractions.md` 的回填建议

当前 `documents/design/language-abstractions.md` 只有 Script 的文法/语义层级和 Localisation 的占位块（见该文件第 50-52 行）。建议按以下结构补齐：

- **Script**：保留现有层级，补充“文件也是成员/定义元素”“Stub 与 Light 的边界”“语义宏（内联脚本/定义注入）与语法宏的区分”。
- **Localisation**：按“属性列表/locale/属性”和“富文本/插值/文法附加结构”两条线组织，明确 `RichText` 不是 `Member`、`PropertyValue` 是惰性富文本容器、参数与命令属于语法宏。
- **CSV**：按“Header/Row/Column”与“行规则/列规则/复杂枚举”组织，明确 CSV 没有成员与宏层级，但列可接入 `ParadoxExpressionElement` 与 `ParadoxComplexExpression`。
- **CWT**：按“成员/选项/注释/值”与“规则数据模型/选项元数据”组织，明确 CWT 是规则语言、其语义层是 `CwtConfigGroup`，而不是 Script 语义层的另一套实现。
- **跨语言共性**：单列 `ParadoxLanguage`/`ParadoxFile`/`ParadoxExpressionElement`/`PsiQuoteAwareElement`/`ParadoxStub` 等共享接口，并标注 CWT 与 CSV 的特例。

## 关键设计决策

### 已实施决策

| 决策 | 主要内容 | 依据 |
| --- | --- | --- |
| 成员/语句/表达式元素分层 | Script 与 CWT 采用 `Member`/`Statement`/`MemberContext`/`ExpressionElement` 的概念分层；Localisation/CSV 按各自结构适配 | `adr/script-psi-interface-redesign.md`、`adr/cwt-psi-interface-redesign.md`、`adr/localisation-psi-interface-redesign.md` |
| Script 文件双重角色 | `ParadoxScriptFile` 同时是文件、成员、成员上下文与定义元素 | `ParadoxScriptFile.kt:10`、`adr/script-psi-interface-redesign.md` |
| 宏的两级区分 | “语法宏”提取为 PSI 接口，“语义宏”留给语义层；参数不强行统一 | `adr/macro.md`、`ParadoxScriptMacro.kt`、`ParadoxLocalisationMacro.kt` |
| 引号策略对象化 | `PsiQuoteAwareElement` 只暴露策略，`QuotePattern` 统一断言/修改逻辑 | `adr/quote-pattern.md`、`core/psi/PsiQuoteAwareElement.kt` |
| Lexer 上下文栈 | Script 与 Localisation 文本 Lexer 使用 `stateStack` + `expectStack`，按需回退状态 | `adr/script-lexer-refactor.md`、`adr/localisation-lexer-refactor.md` |
| 惰性解析 | InlineMath 与 Localisation PropertyValue 使用 `IReparseableElementType`；CWT 选项注释明确不惰性解析 | `ParadoxScriptInlineMathElementType.kt`、`ParadoxLocalisationPropertyValueElementType.kt`、`Cwt.bnf` |
| 分层 Lexer | 高亮时使用 `LayeredLexer` 叠加内联数学、富文本与字符串字面量 Lexer | `ParadoxScriptLexerFactory.kt`、`ParadoxLocalisationLexerFactory.kt`、`ParadoxCsvLexerFactory.kt`、`CwtLexerFactory.kt` |
| 自定义轻量 Stub | `LightLanguageStubDefinition` + `StubRegistryExtension` + `LightStubElementFactory` 生成可索引的 light stub | `lang/index/stubs/**`、`lang/index/ChronicleIndexKeys.kt`、`META-INF/chronicle-lang.xml` |
| 语义 LightElement | 参数、复杂枚举、动态值、修饰符、片段等作为非物理导航/相关项 | `lang/psi/light/**`、`core/psi/light/LightElementBase.kt` |
| 注入边界 | 只对 Script 的字符串与参数实参注入；注入文件按宿主上下文参与语义解析 | `lang/injection/**`、`ParadoxPsiFileMatchService.kt` |

### 设计要点解释

- **为什么不统一 `Member`/`Macro`**：四门语言的结构差异较大。CWT 无参数与插值，CSV 无成员层级，Localisation 的富文本不是成员；强行统一会产生大量空实现或语义错位。因此共享的是更底层的能力（文件、表达式、引号、引用）而不是上层结构。
- **为什么 CWT 不进入 `ParadoxLanguage` 基座**：CWT 是规则语言，其语义目标是规则数据模型而不是游戏数据；CWT 语言本身不需要 `ParadoxFile`、定义/封装变量语义或游戏文件 GameType 上下文（规则分组仍按游戏类型组织）。保持独立可以避免 `config` 模块对 `lang` 的进一步依赖。
- **为什么 Script 文件是定义元素**：文件级定义（例如 mod 描述符或文件级配置）需要与属性级定义共享 `ParadoxDefinitionElement` 接口，`ParadoxDefinitionSource.File` 正是为此存在。
- **为什么 Stub 与 AST 双模式**：PSI 接口声明为 `StubBasedPsiElement` 可以让同一实现类同时服务解析树与 light stub 索引；但当前元素类型工厂不创建 AST stub 类型，跨文件索引主要依赖 `LightLanguageStubDefinition`，因此两者的职责边界需要在设计文档中明确。

## 现存待演进项

### 文档与接口层

- **补齐 `language-abstractions.md`**：当前只有 Script 与 Localisation 占位，缺少 Localisation 细节、CSV、CWT 与跨语言共性；本报告第 6 节可作为回填底稿。
- **明确 Stub 双模式**：`ParadoxScriptElementTypeFactory` 与 `ParadoxLocalisationElementTypeFactory` 中 StubElementType 分支被注释，但生成接口仍声明 `StubBasedPsiElement`；需要明确“AST stub 是否保留”“light stub 与 AST stub 的职责边界”。
- **定义 LightElement 缺口**：`ideas.md:434-436` 提到内联定义的 `definitionLightElement`，当前代码中未找到对应实现；现有定义语义主要由 `ParadoxDefinitionInfo` 与 Script PSI 承载。
- **跨语言宏/参数的进一步收敛**：`adr/macro.md` 明确暂不引入共用 `ParadoxParameter`；如果未来要统一，需要先处理 Script/Localisation 参数变体、默认值、tagged parameter 与实参注入的差异。

### Script 语法与语义

- **内联数学条件化块**：`ParadoxScriptConditionalBlock.kt:15` 留有 `TODO 3.0.2+ introduce ParadoxScriptInlineMathConditionalBlock`，`UnsupportedConditionalBlockInspection.kt:41` 也有对应 TODO；`adr/script-syntax-misc.md` 将其列为搁置项。
- **通配参数 `[?PARAM]`**：`proposals/minor-syntax-enhancement.md` 计划引入 `ParadoxScriptWildcardParameter` 并实现 `ParadoxScriptConditionParameter`；当前代码中未找到该类。
- **颜色词元拆分**：`ParadoxScript.bnf` 的 color 规则仍有 TODO；`proposals/minor-syntax-enhancement.md` 计划将颜色/颜色字段切分为更具体的词元，并补颜色块参数检查。
- **命名统一**：`colorfulText`/`textIcon`/`textFormat` 的 PSI 命名、词元命名与 AST 结构尚未统一，`ideas.md` 提出可能将 `colorfulText` 改为 `textColor` + `textColorText`。
- **语法错误抑制配置**：`adr/script-syntax-misc.md` 与 `proposals/minor-syntax-enhancement.md` 都提到可在 conditional block body、argument 首尾抑制语法错误，并提供高亮配置分组。
- **Lexer 重置与增量高亮**：`adr/lexer-reset.md` 仍标记为“部分实施”，Script 与 Localisation 文本 Lexer 的上下文清空、增量重扫与高亮更新仍需持续验证。

### Localisation 语法与语义

- **参数接口拆分**：`adr/localisation-syntax-grammatical.md` 计划将 `ParadoxLocalisationParameter` 重命名为 `ParadoxLocalisationNormalParameter`，并引入统一的 `ParadoxLocalisationParameter`，下分 Normal 与 Tagged；当前只有 `ParadoxLocalisationParameter` 与 `ParadoxLocalisationTaggedParameter`。
- **标签/上下文标签语义化**：计划将 `Tag`/`ContextTag` 作为动态引用，补标签数量检查（不超过 8 个）、快速文档与查找用法；当前仍以文法层兼容为主。
- **字符串变体语义**：`StringVariantSet`/`StringVariant` 已进入文法层，但 `isSpecialLocalisation`、渲染、相关本地化等语义逻辑仍需完善。
- **特殊本地化命令/格式/文本图标**：Issue #137 仍在跟踪 VIC3/CK3 的特殊本地化命令、格式化与文本图标；这与 `CwtLocalisationCommandConfig`、`TextFormat`/`TextIcon` 的语义支持相关。
- **界面文件（GUI）支持**：Issue #109 与 #433 仍在跟踪 VIC3/CK3/EU5 的 GUI 文件映射；这不是 PSI 接口重构本身，但会影响 Script 文法/语义边界的扩展。

### CSV 与 CWT

- **CSV 无 Stub**：当前 CSV 依赖行规则与文件索引；若未来需要在 CSV 中声明定义或做更重的跨文件名称解析，需要评估是否引入 Stub（`adr/csv-and-row-config-support.md` 中“在 CSV 中声明定义”仍是未实施项）。
- **CSV 列语义**：`## declare_complex_enum` 已支持复杂枚举值；定义声明、列规则与脚本表达式的进一步统一仍在演进。
- **CWT 规则系统**：CWT 的语义层主要依赖 `CwtConfigGroup` 与规则处理器；如果未来需要更细的 CWT PSI 语义（如规则符号引用、规则继承的 PSI 级导航），需要在不污染游戏语言模块的前提下扩展 `CwtConfigLightElement` 或新增能力接口。
- **CWT 注释与文档**：`CwtDocComment` 已实现 `PsiDocCommentBase`，但 Markdown 文档、快速文档与补全展示的细节仍可按规则系统演进。

## 关联 GitHub Issue 索引

以下 Issue 与本报告的 PSI/文法/抽象主题直接相关，状态为 2026-11-27 查询结果。

| Issue | 状态 | 标题 | 关联点 |
| --- | --- | --- | --- |
| [#60](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/60) | OPEN | Issue with escaped characters inside multi line strings | Script 字符串/转义与多行文本的词法边界 |
| [#194](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/194) | OPEN | [stellaris]需要支持 在内联中设置 key 作为参数时，能正常索引 引用内联时设置的 key | 内联脚本、参数上下文与索引 |
| [#251](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/251) | CLOSED | 支持在规则文件中通过特定的选项注释注入规则 | CWT 选项注释与规则注入 |
| [#252](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/252) | CLOSED | 支持 VIC3 和 EU5 中的定义注入特性 | Script 语义宏：定义注入 |
| [#263](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/263) | OPEN | [Vic3/EU5] Parser Breaks in CoA | 脚本文法/规则匹配导致的解析边界问题 |
| [#314](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/314) | CLOSED | [CK3] CSV issues | CSV 列规则、复杂枚举值与列索引 |
| [#331](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/331) | CLOSED | [Stellaris] Compatible with `? =` (safe call assign operator) | Script 分隔符词法与 `SAFE_CALL_ASSIGN_SIGN` |
| [#386](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/386) | CLOSED | Missing error on invalid syntax which can cause cascading errors in-game | Script 块内无效语法的检查与恢复 |
| [#109](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/109) | OPEN | [VIC3/CK3] Support for GUI files | Script 文件类型/文法边界扩展 |
| [#137](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/137) | OPEN | [VIC3/CK3] Support special localizations | Localisation 命令/格式化/文本图标的语义支持 |
| [#433](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/433) | OPEN | [CWT / Feature Request] Allow us to map GUI files in CWT | CWT 规则映射与语法扩展 |

补充说明：

- 未检索到专门以“PSI 接口重构”为主题的 Issue；相关决策主要由 `adr/*-psi-interface-redesign.md` 记录。
- `documents/notes/ideas.md:34` 仍保留“新增参考笔记：PSI 接口架构”的待办；本报告可作为该笔记的第一版底稿。
- `documents/notes/proposals/data-api.md` 提出 `ParadoxData`/`ParadoxDataSource`，目标之一是降低语义层对 PSI 的直接绑定；如果实施，会进一步影响“PSI 语义层”与“数据 API”的边界。

## 结论与下一步

- 当前 PSI 架构的核心特征是“少量跨语言能力接口 + 各语言独立结构 + 语义层外挂”。这种设计在 Script/CWT 的成员模型、Localisation 的富文本模型、CSV 的列模型之间保持了必要的差异，但也导致跨语言统一需要依赖扩展属性、Manager 与 LightElement，而不是统一接口。
- 短期最值得补齐的是文档层：将本报告第 3-6 节的内容回填到 `documents/design/language-abstractions.md`，并单列“Stub/Light/Injected 边界”与“四语言对照表”。
- 中期最值得明确的是实现层边界：Stub 双模式（AST stub 与 light stub）、定义 LightElement 缺口、Localisation 参数接口拆分、Script 内联数学条件化块与颜色词元拆分。
- 长期可以评估 `ParadoxData`/`ParadoxDataSource` 是否会成为语义层与 PSI 层之间的稳定边界，从而降低语言功能对具体 PSI 类型的耦合。

## 附录：核验记录与关键路径

- MCP 核验：使用 `intellij-index` 的 `ide_type_hierarchy` 核验了 `ParadoxScriptMember`、`ParadoxLocalisationRichText`、`CwtMember`、`ParadoxCsvColumn`、`ParadoxLanguage` 的层级；使用 `ide_find_implementations` 核验了 `ParadoxExpressionElement`、`PsiQuoteAwareElement` 的实现范围。
- 生成 PSI 路径：`src/main/gen/icu/windea/pls/script/psi`、`src/main/gen/icu/windea/pls/localisation/psi`、`src/main/gen/icu/windea/pls/csv/psi`、`src/main/gen/icu/windea/pls/cwt/psi`。
- 手写 PSI 路径：`src/main/kotlin/icu/windea/pls/script/psi`、`src/main/kotlin/icu/windea/pls/localisation/psi`、`src/main/kotlin/icu/windea/pls/csv/psi`、`src/main/kotlin/icu/windea/pls/cwt/psi`。
- 文法路径：`src/main/kotlin/icu/windea/pls/script/ParadoxScript.bnf`、`ParadoxScript.flex`、`ParadoxScript.InlineMath.flex`；`src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.bnf`、`ParadoxLocalisation.flex`、`ParadoxLocalisation.Text.flex`；`src/main/kotlin/icu/windea/pls/csv/ParadoxCsv.bnf`、`ParadoxCsv.flex`；`src/main/kotlin/icu/windea/pls/cwt/Cwt.bnf`、`Cwt.flex`。
- ParserDefinition：`ParadoxScriptParserDefinition.kt`、`ParadoxLocalisationParserDefinition.kt`、`ParadoxCsvParserDefinition.kt`、`CwtParserDefinition.kt`。
- 存根与索引：`src/main/kotlin/icu/windea/pls/lang/index/stubs/**`、`src/main/kotlin/icu/windea/pls/lang/index/ChronicleIndexKeys.kt`、`src/main/resources/META-INF/chronicle-lang.xml`。
- 注入：`src/main/kotlin/icu/windea/pls/lang/injection/**`、`src/main/kotlin/icu/windea/pls/lang/psi/ParadoxLanguageInjectionHost.kt`。
- 参考文档：`docs/zh/ref-syntax.md`、`docs/en/ref-syntax.md`、`documents/design/language-abstractions.md`、`documents/notes/guidelines.md`。
- ADR：`cwt-psi-interface-redesign.md`、`script-psi-interface-redesign.md`、`localisation-psi-interface-redesign.md`、`macro.md`、`quote-pattern.md`、`lexer-reset.md`、`script-lexer-refactor.md`、`localisation-lexer-refactor.md`、`localisation-syntax-grammatical.md`、`script-syntax-misc.md`、`csv-and-row-config-support.md`。
