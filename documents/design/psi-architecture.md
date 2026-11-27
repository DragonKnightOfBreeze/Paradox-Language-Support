# PSI 接口架构规范

> - 版本：`v3.1.0`
> - 更新日期：`2026-11-28`
> - 状态：**现行活规范（Living Specification）**
> - 文档定位：记录 Paradox Chronicle 插件支持的四门语言（Paradox Script、Paradox Localisation、Paradox CSV、CWT）在 IntelliJ 平台下的 PSI 分层模型、接口继承谱系、跨语言能力接口、存根（Stub）、轻量元素（LightElement）与语言注入边界，以及关键设计决策与演进约束。
> - 互链说明：文法层与语义层的具体语言构造定义详见 [`language-abstractions.md`](language-abstractions.md)；复杂表达式轻量语法树详见 [`complex-expressions.md`](complex-expressions.md)；脚本片段与代码围栏注入机制详见 [`script-injection.md`](script-injection.md)。

---

## 一、总体分层模型与架构原则

### 1.1 总体分层模型

Paradox Chronicle 的 PSI 基础设施采用“Lexer → Parser/AST → 生成 PSI 接口与实现 → 手写混入能力接口 → 语义层接入”的标准分层架构。四门语言虽然各自文法差异巨大，但在物理 AST 到语义能力的映射上遵循一致的分层模式：

```text
+--------------------------------------------------------------------------+
| 语义层 (icu.windea.pls.lang / config)                                     |
| definition / expression / complexExpression / macro / references /       |
| light elements / stubs / config groups                                   |
+--------------------------------------------------------------------------+
| 手写 PSI 混入能力接口 (src/main/kotlin/icu/windea/pls/{lang,core,*/psi}) |
| Member / Statement / RichText / Macro / Interpolation / QuoteAware ...   |
+--------------------------------------------------------------------------+
| 生成 PSI 接口与实现 (src/main/gen/icu/windea/pls/*/psi)                  |
| ParadoxScript* / ParadoxLocalisation* / ParadoxCsv* / Cwt*               |
+--------------------------------------------------------------------------+
| 平台解析底座 (ParserDefinition / Parser / Lexer)                         |
| Grammar-Kit 生成解析器 / JFlex 词法分析器 / 延迟解析 / FileType / Language|
+--------------------------------------------------------------------------+
```

1. **平台解析底座**：由 JFlex 生成的词法分析器（`*.flex`）和 Grammar-Kit 生成的语法分析器（`*.bnf`）构成，生成代码位于 `src/main/gen`。负责将物理字符流转换为带 `IElementType` 的抽象语法树（AST）。
2. **生成 PSI 层**：由 Grammar-Kit 自动生成各语言的基础 PSI 接口与实现类，接口通常派生自 `com.intellij.psi.PsiElement`，实现类派生自 `com.intellij.extapi.psi.ASTWrapperPsiElement`，文件类派生自 `com.intellij.extapi.psi.PsiFileBase`。
3. **手写混入能力接口**：位于 `src/main/kotlin/icu/windea/pls/{cwt,script,localisation,csv}/psi` 以及跨语言的 `core/psi` 和 `lang/psi` 中。通过在 BNF 中声明 `implements` 或重载生成类，赋予生成节点通用的 AST 遍历、引号识别、边界检测、命名元素和引用能力。
4. **语义层接入**：插件核心语义模型（定义信息 `definitionInfo`、复杂表达式 `complexExpression`、动态值、作用域上下文）并非全部建模为强类型 PSI 子类，而是通过扩展属性、缓存管理器（Manager/Service）、引用提供者（PsiReferenceProvider）和轻量元素（LightElement）非侵入地挂接在 PSI 节点之上。

### 1.2 词法分析与延迟解析边界

各语言根据语法复杂性与性能要求，采用了不同的词法处理与延迟解析策略：

| 语言 | 词法结构 | 上下文栈机制 | 延迟解析 / 分层机制 | 设计说明与 ADR 依据 |
| :--- | :--- | :--- | :--- | :--- |
| **Paradox Script** | `ParadoxScript.flex` + `ParadoxScript.InlineMath.flex` | `stateStack` + `expectStack` | `INLINE_MATH_TOKEN` 经 `ParadoxScriptInlineMathElementType` 延迟解析为独立语法树；高亮时叠加内联数学与字符串 Lexer。 | 避免在顶层 Lexer 中混合复杂的数学表达式状态机。参见 `documents/notes/adr/script-lexer-refactor.md`。 |
| **Paradox Localisation** | `ParadoxLocalisation.flex`（文件结构） + `ParadoxLocalisation.Text.flex`（富文本） | `stateStack` + `expectStack` + `ParadoxSyntaxConstraint` | `PROPERTY_VALUE_TOKEN` 经 `ParadoxLocalisationPropertyValueElementType` 延迟解析为 `TEXT_ROOT`；纯文本提供快速路径。 | 本地化属性值富文本语法繁复，拆分为两级 Lexer 可大幅降低全文件索引与扫描开销。参见 `documents/notes/adr/localisation-lexer-refactor.md`。 |
| **Paradox CSV** | `ParadoxCsv.flex`（单层） | `EOL` 状态切换与边界检查 | 无延迟解析；高亮时叠加字符串字面量 Lexer。 | 结构简单平铺，依赖行规则（`CwtRowConfig`）进行语义绑定。参见 `documents/notes/adr/csv-and-row-config-support.md`。 |
| **CWT** | `Cwt.flex`（单层，含选项状态） | `IN_OPTION` / `IN_OPTION_VALUE` / `IN_OPTION_NESTED` | **明确不采用延迟解析**；高亮时叠加字符串字面量 Lexer。 | CWT 是核心规则库，规则文件体量大且频繁访问。基准测试表明保留在单层词法扫描中性能更优。参见 `Cwt.bnf` 历史决策。 |

---

## 二、四门语言的 PSI 接口继承谱系

### 2.1 Paradox Script PSI 谱系

Paradox Script 是游戏逻辑的核心载体。其 PSI 体系建立在清晰的“文件 / 成员 / 语句 / 值 / 宏”概念划分之上：

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
      │  │  │  implements ParadoxScriptNamedElement, StubBasedPsiElement<ParadoxScriptPropertyStub>,
      │  │  │             ParadoxDefinitionElement, NavigatablePsiElement
      │  │  └─ ParadoxScriptValue
      │  ├─ ParadoxScriptScriptedVariable
      │  │  implements ParadoxScriptNamedElement, StubBasedPsiElement<ParadoxScriptScriptedVariableStub>,
      │  │             NavigatablePsiElement
      │  └─ ParadoxScriptNormalConditionalBlock
      │     implements ParadoxScriptConditionalBlock, ParadoxScriptMemberContainer
      └─ (comment / 空白等)
```

值（Value）与字面量表达式节点：

```text
ParadoxScriptExpressionElement
├─ ParadoxScriptPropertyKey
│  implements ParadoxScriptLiteralValue, ParadoxScriptStringExpressionElement,
│             ParadoxScriptInterpolationContainer, PsiQuoteAwareElement
└─ ParadoxScriptValue
   implements ParadoxScriptMember
   ├─ ParadoxScriptBoolean
   ├─ ParadoxScriptNumber (ParadoxScriptInt, ParadoxScriptFloat)
   ├─ ParadoxScriptString
   │  implements ParadoxScriptLiteralValue, ParadoxScriptStringExpressionElement,
   │             ParadoxScriptInterpolationContainer, ParadoxLanguageInjectionHost,
   │             PsiQuoteAwareElement
   ├─ ParadoxScriptBlock
   │  implements ParadoxScriptMemberContainer, ParadoxScriptBoundMemberContainer,
   │             PsiBoundElement, PsiListLikeElement
   ├─ ParadoxScriptColor
   ├─ ParadoxScriptScriptedVariableReference
   │  implements ParadoxScriptMacro, ParadoxScriptedVariableReference
   └─ ParadoxScriptInlineMath
      implements ParadoxScriptMacro, PsiBoundElement
```

语法宏与插值体系：

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

- **文件双重角色**：`ParadoxScriptFile` 既是 `PsiFile`，又实现了 `ParadoxScriptMember`、`ParadoxScriptMemberContext` 与 `ParadoxDefinitionElement`。这使“整个文件作为一个定义（File-as-Definition）”的场景（如通用命名空间的事件脚本或独立定义文件）能够无缝融入定义解析体系。参见 `documents/notes/adr/script-psi-interface-redesign.md`。
- **内联数学根节点**：`ParadoxScriptInlineMathRoot` 作为 BNF 的 `extraRoot`，由 `INLINE_MATH_TOKEN` 延迟解析产生，下辖一元、二元、分组、绝对值表达式及数值/封装变量因子。

### 2.2 Paradox Localisation PSI 谱系

本地化语言采用“平铺属性列表 + 延迟富文本容器”模型，不设通用的 `Member` 接口：

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

富文本与语法宏节点：

```text
ParadoxLocalisationRichText
├─ ParadoxLocalisationText
├─ ParadoxLocalisationColorfulText (含颜色标记 §...§!)
├─ ParadoxLocalisationParameter (含 $...$ 插值)
├─ ParadoxLocalisationCommand (含 [...] 命令)
├─ ParadoxLocalisationConceptCommand (概念命令)
├─ ParadoxLocalisationIcon (含 £...£ 贴图)
├─ ParadoxLocalisationTextIcon (文本图标)
└─ ParadoxLocalisationTextFormat (文本格式化)

ParadoxLocalisationMacro
├─ ParadoxLocalisationInterpolation (Parameter, Command)
├─ ParadoxLocalisationScriptedVariableReference ($@var$)
└─ ParadoxLocalisationTaggedParameter (<PARAM>)
```

- **结构与内容解耦**：`ParadoxLocalisationPropertyList`、`Locale` 与 `Property` 负责构建文件的键值结构与语言区域索引；`PropertyValue` 则作为富文本容器，内部承载复杂的语法宏、图标、颜色与命令。
- **参数与命令模型**：`ParadoxLocalisationParameter` 与 `ParadoxLocalisationCommand` 共同实现 `ParadoxLocalisationInterpolation`，代表可内嵌插值的语法宏；命令内部文本进一步通过复杂表达式模型解析。

### 2.3 Paradox CSV PSI 谱系

Paradox CSV 用于表达表格化数据，文法结构非常简洁：

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

- **单层表达式承载**：CSV 仅有 `Header`、`Row` 与 `Column`。`Column` 是唯一的表达式载体，同时实现 `ParadoxCsvExpressionElement` 与平台 `PsiLiteralValue`，从而接入表达式解析与引用系统。
- **无节点存根**：CSV 依赖 `CwtRowConfig` 驱动的行规则匹配，列与表头关系由 `ParadoxCsvPsiService` 动态维护，不构建节点级 Stub。

### 2.4 CWT (Config World Tree) PSI 谱系

CWT 是驱动整套插件语义智能的元规则描述语言：

```text
PsiFile
└─ CwtFile
   implements CwtMemberContext
   └─ CwtRootBlock
      implements CwtMemberContainer, PsiRootBlock, PsiListLikeElement
      ├─ CwtMember
      │  ├─ CwtProperty
      │  │  implements CwtNamedElement, PsiPresentableTextAwareElement, NavigatablePsiElement
      │  └─ CwtValue
      │     implements CwtExpressionElement, CwtOptionMember,
      │                PsiPresentableTextAwareElement, NavigatablePsiElement
      ├─ CwtOptionComment (## 选项注释，包含 CwtOption 与 CwtValue)
      └─ CwtDocComment (### 或 #### 文档注释)
```

值与选项体系：

```text
CwtExpressionElement
├─ CwtPropertyKey
│  implements CwtStringExpressionElement, CwtLiteralValue, PsiQuoteAwareElement
└─ CwtValue
   ├─ CwtBoolean
   ├─ CwtNumber (CwtInt, CwtFloat)
   ├─ CwtString
   │  implements CwtNamedElement, CwtStringExpressionElement, CwtLiteralValue, PsiQuoteAwareElement
   └─ CwtBlock
      implements CwtMemberContainer, CwtBoundMemberContainer, PsiListLikeElement

CwtOptionMember
├─ CwtOption
└─ CwtValue
```

- **与 Script 同构但解耦**：CWT 拥有与 Script 类似的 `Member`、`Block`、`Property`、`Value` 层次，但两者类继承完全独立，互不共享领域接口。
- **三级注释系统**：CWT 支持普通单行行注释（`#`）、选项注释（`##`）与文档注释（`###` / `####`）。选项注释直接解析为 `CwtOptionComment`，并在编译后挂接为规则的 `CwtOptionMetadata`。

---

## 三、跨语言公共基接口与能力契约

### 3.1 跨语言基接口谱系对照

| 维度 | Paradox Script | Paradox Localisation | Paradox CSV | CWT |
| :--- | :--- | :--- | :--- | :--- |
| **语言基类** | `ParadoxLanguage` (`ParadoxBaseLanguage`) | `ParadoxLanguage` (`ParadoxBaseLanguage`) | `ParadoxLanguage` (`ParadoxBaseLanguage`) | `com.intellij.lang.Language`（直接继承） |
| **文件基接口** | `ParadoxFile` + `ParadoxFileType` | `ParadoxFile` + `ParadoxFileType` | `ParadoxFile` + `ParadoxFileType` | `PsiFile` + `LanguageFileType` |
| **成员与语句抽象** | `Member` / `Statement` / `MemberContainer` | 扁平模型（无 Member，直接为 PropertyList） | 表格模型（Header / Row / Column） | `CwtMember` / `CwtStatement` / `CwtMemberContainer` |
| **表达式接口** | `ParadoxScriptExpressionElement` | `ParadoxLocalisationExpressionElement` | `ParadoxCsvExpressionElement` | `CwtExpressionElement` |
| **跨语言通用表达式接口** | 实现 `ParadoxExpressionElement` | 实现 `ParadoxExpressionElement` | 实现 `ParadoxExpressionElement` | **不实现** |
| **语法宏接口** | `ParadoxScriptMacro` / `Interpolation` | `ParadoxLocalisationMacro` / `Interpolation` | 无 | 无 |
| **Stub 索引模式** | Light Stub（`StubRegistryExtension` + `LightStubElementFactory`） | Light Stub（`StubRegistryExtension` + `LightStubElementFactory`） | 无 Stub（依赖轻量文件索引） | 无 Stub（直接编译为规则数据模型） |

### 3.2 跨语言通用能力接口

位于 `icu.windea.pls.core.psi` 与 `icu.windea.pls.lang.psi` 的核心公共接口：

1. **`ParadoxExpressionElement`**：
   - 跨三门游戏语言（Script、Localisation、CSV）的顶层表达式抽象。
   - 暴露 `name`、`value`、`setValue(String)`、`setContent(String)` 等通用操作契约。
   - 使统一的引用解析框架（`ParadoxScriptExpressionSupport` 等）与快速文档机制能统一消费不同语言节点的字面量值。
2. **`PsiQuoteAwareElement` 与 `QuotePattern`**：
   - 统一抽象带有引号（双引号或单引号）包裹特性的 AST 节点。
   - 该接口仅暴露策略对象 `quotePattern`，自身不直接提供变异方法。实际的加引号、去引号、引号需求校验等逻辑收敛于 `icu.windea.pls.core.text.QuotePattern`。
   - 各语言按文法特性注入独立策略：`ParadoxScriptQuotePattern`（要求标识符与边界符号受限时加引号）、`ParadoxLocalisationQuotePattern`（默认不加引号）、`ParadoxCsvQuotePattern` 等。参见 `documents/notes/adr/quote-pattern.md`。
3. **`PsiBoundElement` 与 `PsiRootBlock`**：
   - `PsiBoundElement` 表示具有显式左右边界词元（如大括号 `{}`）的代码块（如 `ParadoxScriptBlock`、`CwtBlock`、`ParadoxScriptInlineMath`）。
   - `PsiRootBlock` 表示文件顶层“无边界”的代码块，实现了无显式边界的块容器遍历统一性。
4. **`PsiPresentableTextAwareElement`**：
   - 为各语言节点提供标准化展示文本提取逻辑，供结构视图（Structure View）、代码折叠（Folding）及展示面板统一读取。
5. **`PsiReadWriteAccessAwareElement`**：
   - 赋予 PSI 节点读写访问语义，主要用于复杂枚举值、动态值、参数等轻量元素的只读/写入分析。
6. **`ParadoxLanguageInjectionHost`**：
   - 标识该 PSI 元素能够承载平台语言注入，目前由 `ParadoxScriptString` 与 `ParadoxScriptNormalParameterArgument` 实现。

### 3.3 CWT 刻意独立于 `ParadoxLanguage` 基座的设计依据

在架构设计上，CWT 明确**不继承** `ParadoxLanguage`，也不实现 `ParadoxFile` 或 `ParadoxExpressionElement`：

1. **元模型与领域模型的分立**：Paradox Script/Localisation/CSV 是 Paradox 游戏引擎运行时直接消费的“领域数据与脚本”；而 CWT 是仅在开发期被插件用来驱动代码补全、检查、导航的“类型与模式规则（Schema）”。
2. **表达式语义完全不同**：Script 中的表达式是游戏逻辑、变量或作用域字段；CWT 中的表达式（如 `scope_field`、`alias_keys_field`、`$define_reference`）是**规则表达式类型元信息**。若混入统一表达式层级，将导致类型系统混乱和多态分派膨胀。
3. **生命周期与映射策略解耦**：游戏语言需要通过 `ParadoxRootInfo` 与 `ParadoxFileInfo` 结合游戏目录结构识别文件类型；而 CWT 文件类型固定（`*.cwt`），解析产物直接转化为只读的规则缓存容器 `CwtConfigGroup`，不参与游戏定义索引构建。

---

## 四、Stub、Light 与 Injected 边界模型

### 4.1 存根体系（Stub Architecture）的现代 Light Stub 实现

插件对跨文件符号索引采用平台现代存根 API 提供的 Light Stub 机制，而不是 `IStubElementType` / `ILightStubElementType` 的旧存根元素类型方案（其中 `ILightStubElementType` 已标注 `@ApiStatus.Obsolete`）：

```text
[物理源码]
   ├─ 索引期
   │    LightLanguageStubDefinition.parseContentsLight
   │      → LightStubBuilder
   │      → [Light Stub 树] ──(StubSerializer.indexStub / IndexSink)──> [StubIndex / ChronicleIndexKeys]
   │
   └─ 分析期
        [StubIndex 查询] → [Light Stub 树] ──(LightStubElementFactory.createPsi(stub))──> [StubBasedPsiElement]
                                                                                          │
                                                                                          └─(按需 getNode())→ [AST]
```

1. **核心存根接口**：
   - `ParadoxStub<T>`：存根顶层契约，必须携带 `gameType`，支持自顶向下与向父级安全转换。
   - `ParadoxLocaleAwareStub<T>`：针对本地化语言增加 `locale` 语言区域信息。
2. **存根类型覆盖**：
   - **Script**：文件级存根 `ParadoxScriptFileStub`；节点级存根包括 `ParadoxScriptPropertyStub`（以 sealed 类建模定义、定值变量、内联脚本用法与参数）和 `ParadoxScriptScriptedVariableStub`。
   - **Localisation**：文件级存根 `ParadoxLocalisationFileStub`；节点级存根包括 `ParadoxLocalisationPropertyListStub`、`ParadoxLocalisationLocaleStub`、`ParadoxLocalisationPropertyStub`。
3. **现代平台存根注册路径**：
   - **语言级定义**：`ParadoxScriptStubDefinition` 与 `ParadoxLocalisationStubDefinition` 实现 `LightLanguageStubDefinition`，通过 `META-INF/chronicle-lang.xml` 的 `languageStubDefinition` 注册，提供 `stubVersion`、`LightStubBuilder`（`ParadoxScriptStubBuilder` / `ParadoxLocalisationStubBuilder`）、`shouldBuildStubFor` 与 `parseContentsLight`。
   - **节点级工厂与序列化器**：`ParadoxScriptStubRegistry` 与 `ParadoxLocalisationStubRegistry` 实现 `StubRegistryExtension`，通过 `stubElementRegistryExtension` 注册；它们为文件元素类型注册 `StubSerializer`，并为 `SCRIPTED_VARIABLE`/`PROPERTY`（以及 `PROPERTY_LIST`/`LOCALE`/`PROPERTY`）注册 `LightStubElementFactory` 与 `StubSerializer`。
   - **构建与反序列化**：`LightStubBuilder` 在索引期从 LighterAST 调用 `LightStubElementFactory.createStub(tree, node, parentStub)`，工厂同时提供 `createStub(psi, parentStub)`；反序列化后的节点存根通过 `StubBase.getPsi()` 调用 `createPsi(stub)`，生成 `StubBasedPsiElement` 实现类（如 `ParadoxScriptPropertyImpl(stub, PROPERTY)`）。
4. **无 Stub 语言**：CSV 与 CWT 不生成 Stub。CSV 通过轻量行规则与文件索引提供支持；CWT 规则文件直接解析为内存中的 `CwtConfigGroup`。

### 4.2 轻量元素体系（LightElement Architecture）

当一个符号在物理源码中并不存在显式的单一定义节点，或者该符号是从规则、动态评估或复合结构中派生而来时，插件统一使用 LightElement 进行建模：

```text
com.intellij.psi.impl.light.LightElement
└─ icu.windea.pls.core.psi.light.LightElementBase
   ├─ icu.windea.pls.lang.psi.light.ParadoxLightElementBase (游戏语义)
   │  ├─ ParadoxParameterLightElement (脚本参数声明/解析结果)
   │  ├─ ParadoxLocalisationParameterLightElement (本地化参数)
   │  ├─ ParadoxComplexEnumValueLightElement (动态枚举/复杂枚举值)
   │  ├─ ParadoxDynamicValueLightElement (动态值：event_target, variable, flag 等)
   │  ├─ ParadoxModifierLightElement (动态生成的修饰符)
   │  ├─ ParadoxDefinitionSnippetLightElement (定义名称片段引用)
   │  ├─ ParadoxLocalisationSnippetLightElement (本地化名称片段引用)
   │  ├─ ParadoxShaderEffectLightElement / ParadoxMeshLocatorLightElement (图形引用)
   │  └─ ParadoxModifierCategoryLightElement (修饰符分类)
   └─ icu.windea.pls.lang.psi.light.CwtConfigLightElementBase (规则语义)
      ├─ CwtConfigSymbolLightElement (CWT 规则符号)
      └─ CwtMemberConfigLightElement (规则成员项)
```

- **契约边界**：LightElement 不是物理 AST 节点，默认不分配独立的物理 `TextRange`；其 `containingFile` 代理自产生它的上下文宿主文件。
- **职责定位**：LightElement 作为只读的“语义符号代理”，实现 `PsiNamedElement` 与 `NavigatablePsiElement`，使动态符号能够自然支持查找用法（Find Usages）、快速文档（Quick Doc）、重命名（Rename）和代码补全。
- **无定义 LightElement**：脚本中的“定义（Definition）”始终绑定在具体的物理 AST 节点（`ParadoxScriptProperty` 或 `ParadoxScriptFile`）之上，辅以 `definitionInfo` 扩展属性，当前不存在 `ParadoxDefinitionLightElement`。

### 4.3 语言注入边界（Injected PSI Boundaries）

语言注入使得特定文本片段能够在物理宿主内部作为独立的子语言 PSI 树被解析：

1. **物理注入宿主**：严格限定为实现 `ParadoxLanguageInjectionHost` 的节点，即 `ParadoxScriptString`（参数实参、富文本）与 `ParadoxScriptNormalParameterArgument`（参数默认值 `$PARAM|DEFAULT$`）。
2. **规则上下文语义注入（非二次物理注入）**：注入得到的内部文件通过 `CwtConfigContextProvider` 扩展点按优先级寻找匹配的上下文提供者，进而获取 `CwtConfigContext`，提供代码补全和高亮。
3. **顶层宿主回溯**：由于平台不支持嵌套语言注入，当注入的脚本片段内部再次访问外部作用域或外层内联脚本时，插件通过 `ParadoxLanguageInjectionManager.findTopHostElementOrThis` 与 `findTopHostFileOrThis` 沿 `VirtualFileWindow.delegate` 递归回溯，恢复其物理宿主所在的文件与定义上下文。详细机制参见 [`script-injection.md`](script-injection.md)。

---

## 五、关键设计决策与未来演进界线

### 5.1 关键已决设计决策（ADR 索引）

| 架构决策 | 核心契约与事实 | 关联 ADR / 记录 |
| :--- | :--- | :--- |
| **四门语言独立分层** | Script 与 CWT 在 PSI 语法结构同构的前提下，严格保持独立的接口定义与类型体系；CWT 不进入 `ParadoxLanguage` 基座。 | `adr/script-psi-interface-redesign.md`<br>`adr/cwt-psi-interface-redesign.md` |
| **宏的两级架构划分** | 语法上可确认的抽象（参数、条件化块、内联数学、封装变量引用）由 PSI 接口（`ParadoxScriptMacro` 等）直接建模；语义上的调用扩展（内联脚本、定义注入）由语义层驱动。 | `adr/macro.md` |
| **参数接口不强行统一** | Script 参数与 Localisation 参数由于语法符号、转义规则与变体支持差异巨大，分别使用 `ParadoxScriptParameter` 与 `ParadoxLocalisationParameter`。 | `adr/macro.md` |
| **统一引号策略抽象** | 将引号增删与判定逻辑从 PSI 节点剥离，由 `PsiQuoteAwareElement` 暴露只读策略，统一委托给 `QuotePattern` 引擎。 | `adr/quote-pattern.md` |
| **CWT 选项注释禁用延迟解析** | 考虑到 CWT 规则文件加载频次与解析开销，选项注释（`##`）保持在顶层单层词法扫描中解析，不引入复杂的延迟子树。 | `Cwt.bnf` 历史决策 |
| **现代 Light Stub 存根注册** | 以 `LanguageStubDefinition` / `LightLanguageStubDefinition`、`StubRegistryExtension`、`LightStubBuilder`、`LightStubElementFactory` 注册普通 `IElementType`；不再使用 `IStubElementType` / `ILightStubElementType`。 | `lang/index/stubs/**`、`META-INF/chronicle-lang.xml` |

### 5.2 未来演进界线（Out of Scope for v3.1.x）

以下内容属于未来架构蓝图或待定技术提案，**不属于当前现行事实**：

1. **Localisation 语法标签全面语义化**：当前 `ParadoxLocalisationTag`、`ContextTag`、`StringVariantSet` 主要停留在文法和高亮层，未来演进拟引入动态规则引用与校验。参见 `documents/notes/adr/localisation-syntax-grammatical.md`。
2. **内联定义的 LightElement 抽象**：对于在同一块内通过宏或复合属性动态派生的内联定义，当前仍依赖静态定义解析；尚未引入一等公民级的定义轻量元素。
