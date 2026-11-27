# 语言抽象设计规范

> - 版本：`v3.1.0`
> - 更新日期：`2026-11-28`
> - 状态：**现行活规范（Living Specification）**
> - 文档定位：规范各语言（Paradox Script、Paradox Localisation、Paradox CSV、CWT）在文法级别（Grammar Level / 语法抽象）与语义级别（Semantic Level / 领域语义）的抽象层级、领域概念与 PSI 模型对应关系。
> - 互链说明：底层 PSI 接口继承树与 Stub/LightElement 机制详见 [`psi-architecture.md`](psi-architecture.md)；轻量语法树复杂表达式详见 [`complex-expressions.md`](complex-expressions.md)；脚本片段与代码围栏注入机制详见 [`script-injection.md`](script-injection.md)。

---

## 一、脚本文件 (Paradox Script)

### 1.1 文法级别 (Grammar Level)

Paradox Script 的语法结构围绕“成员（Member）”与“语句（Statement）”展开，支持嵌套块、封装变量、参数化结构以及内联数学计算：

- **文件与根块**
  - 脚本文件 (`ParadoxScriptFile`)：实现了 `ParadoxScriptMemberContext`、`ParadoxScriptMember` 与 `ParadoxDefinitionElement`，可承载整个文件级别的定义。
  - 根块 (`ParadoxScriptRootBlock`)：顶层无括号包裹的无边界成员容器。
- **封装变量** (`ParadoxScriptScriptedVariable`)：脚本文件内以 `@var = value` 声明的变量。
- **成员与语句** (`ParadoxScriptMember` / `ParadoxScriptStatement`)
  - **属性** (`ParadoxScriptProperty`)：由属性键与属性值组成，支持多种分隔符（`=`、`<`、`>`、`!=` 等）。
    - 属性键 (`ParadoxScriptPropertyKey`)：字符串字面量或参数化键。
  - **值** (`ParadoxScriptValue`)
    - 布尔值 (`ParadoxScriptBoolean`)
    - 数字 (`ParadoxScriptNumber`：`ParadoxScriptInt`、`ParadoxScriptFloat`)
    - 字符串 (`ParadoxScriptString`)：支持带引号与不带引号文本，可承载语言注入。
    - 颜色 (`ParadoxScriptColor`：RGB/HSV 颜色声明)
    - 封装变量引用 (`ParadoxScriptScriptedVariableReference`)
    - 内联数学 (`ParadoxScriptInlineMath`)
    - 代码块 (`ParadoxScriptBlock`)：以大括号包裹的成员容器。
- **语法宏体系** (`ParadoxScriptMacro`)
  - **参数相关**
    - （普通）参数 (`ParadoxScriptNormalParameter`，如 `$PARAM$`)
    - 内联数学参数 (`ParadoxScriptInlineMathParameter`)
    - 参数实参 (`ParadoxScriptNormalParameterArgument`、`ParadoxScriptInlineMathParameterArgument`)
  - **条件化块相关**
    - （普通）条件化块 (`ParadoxScriptNormalConditionalBlock`，如 `[[PARAM] ... ]`)
    - 内联条件化块 (`ParadoxScriptInlineConditionalBlock`)
    - 条件化表达式 (`ParadoxScriptConditionalExpression`)
    - 条件化参数 (`ParadoxScriptConditionalParameter`)
  - **内联数学相关**
    - 内联数学根 (`ParadoxScriptInlineMathRoot`)：延迟解析的独立子树。
    - 内联数学表达式 (`ParadoxScriptInlineMathExpression`)
    - 内联数学因子 (`ParadoxScriptInlineMathFactor`)：包括数字、封装变量引用与参数。

### 1.2 语义级别 (Semantic Level)

在语义层，文法节点根据上下文规则被映射为不同的游戏领域概念：

- **封装变量** (`scriptedVariable`)
  - 本地封装变量（`Local`）：仅在当前文件内部生效。
  - 全局封装变量（`Global`）：位于 `common/scripted_variables`，跨文件全局生效。
- **定义** (`definition`)
  - 语义定义是游戏逻辑的核心对象，由 `ParadoxDefinitionInfo` 提供元数据支持。
  - 顶级键 (`rootKey`)：脚本最外层的属性键。
  - 类型键 (`typeKey`)：定义所属的 CWT 类型识别键。
  - 定义来源 (`ParadoxDefinitionSource`)：文件（`File`）、属性（`Property`）、内联脚本（`Inline`）、定义注入（`Injection`）。
  - 定义成员路径 (`memberPath`)：当前属性在定义内部的层级路径。
- **宏分类** (`macro`)
  - **语法宏**（Syntax Macro）：直接在文法与 PSI 层表达的宏构造，包括参数（`$PARAM$`）、条件化块（`[[PARAM] ...]`）、内联数学（`@[...]`）和封装变量引用（`@var`）。
  - **语义宏**（Semantic Macro）：在语义层展开的代码复用机制，包括内联脚本（`inline_script`，主见于 Stellaris）与定义注入（`definition_injection`，主见于 VIC3 / EU5）。
- **复杂表达式** (`complexExpression`)
  - 由 CWT 规则数据类型驱动、在运行时动态解析的轻量语法树，包括作用域字段、值字段、变量字段、动态值、命令、脚本值引用、定值引用、数组定值引用、标签集、数据库对象、命名格式与模板表达式。详见 [`complex-expressions.md`](complex-expressions.md)。
- **定值系统** (`defines`)
  - 定值命名空间 (`ParadoxDefineNamespaceInfo`)：游戏基础常量命名空间。
  - 定值变量 (`ParadoxDefineVariableInfo`)：命名空间下的特定参数键值。
- **动态值与复杂枚举**
  - 动态值（`dynamicValue`）：脚本运行期产生并消费的值（如 `event_target`、`variable`、`flag`）。
  - 复杂枚举值（`complexEnumValue`）：由规则在脚本特定结构中动态扫描提取的枚举项。

### 1.3 存根与轻量元素边界

- **存根（Stub）**：脚本支持文件级存根（`ParadoxScriptFileStub`）以及属性（`ParadoxScriptPropertyStub`，涵盖定义、定值、内联脚本用法）和封装变量（`ParadoxScriptScriptedVariableStub`）的节点存根；存根以普通 `IElementType` 作为元素类型，通过 `LightLanguageStubDefinition` + `StubRegistryExtension` + `LightStubElementFactory` 注册，由 `LightStubBuilder` 从 PSI 或 LighterAST 构建，并在需要时通过 `createPsi(stub)` 还原为 `StubBasedPsiElement`。
- **轻量元素（LightElement）**：参数、动态值、复杂枚举值、动态修正等在脚本代码中不具备独立强类型物理声明的符号，由轻量元素代理提供导航、快速文档与重命名能力。

---

## 二、本地化文件 (Paradox Localisation)

### 2.1 文法级别 (Grammar Level)

Paradox Localisation（`*.yml`）是面向玩家展示文本的多语言资源，其文法呈现“平铺属性列表 + 富文本延迟解析容器”结构：

- **文件与属性列表**
  - 本地化文件 (`ParadoxLocalisationFile`)：顶层本地化文件。
  - 属性列表 (`ParadoxLocalisationPropertyList`)：特定语言环境下的条目集合。
- **语言区域** (`ParadoxLocalisationLocale`)
  - 如 `l_english:`、`l_simp_chinese:`，作为属性列表的领头标记。
- **本地化条目** (`ParadoxLocalisationProperty`)
  - 属性键 (`ParadoxLocalisationPropertyKey`)：本地化文本条目的引用键。
  - 属性值 (`ParadoxLocalisationPropertyValue`)：富文本容器，经延迟词法解析生成内部语法树。
- **富文本体系** (`ParadoxLocalisationRichText`)
  - 普通文本 (`ParadoxLocalisationText`)
  - 多彩文本 (`ParadoxLocalisationColorfulText`)：由颜色代码（`§Y...§!`）包裹的富文本。
  - 本地化参数 (`ParadoxLocalisationParameter`)：`$KEY$` 形式的引用或宏参数，支持传入参数（`|...`）。
  - 本地化命令 (`ParadoxLocalisationCommand`)：`[...]` 形式的数据表达式，可带命令参数与标签（`&...`）。
  - 概念命令 (`ParadoxLocalisationConceptCommand`)：`['concept_name', ...]` 形式的术语概念引用。
  - 图标 (`ParadoxLocalisationIcon`)：`£icon_name£` 形式的贴图精灵引用。
  - 文本图标 (`ParadoxLocalisationTextIcon`) 与 文本格式 (`ParadoxLocalisationTextFormat`)
- **文法附加节点**
  - 标签与上下文标签 (`ParadoxLocalisationTag` / `ParadoxLocalisationContextTag`)
  - 字符串变体与标签 (`ParadoxLocalisationStringVariantSet` / `ParadoxLocalisationStringVariant`)
  - 标签敏感文本与标签化参数 (`ParadoxLocalisationTagSensitiveText` / `ParadoxLocalisationTaggedParameter`：`<PARAM>`)

### 2.2 语义级别 (Semantic Level)

- **本地化条目** (`localisation`)
  - 普通本地化（`Normal`）：按语言环境生效的常规本地化文本。
  - 同步本地化（`Synced`）：跨语言共享的基础文本标识（如国家代码、专有名词）。
  - 语言区域匹配：基于当前编辑环境或首选语言回退链路寻找最佳本地化文本。
- **富文本语义映射**
  - 颜色语义：解析为游戏内预定义的文本颜色（`ParadoxTextColorManager`）。
  - 概念定义：解析为游戏内定义的概念条目，提供悬浮预览。
  - 贴图精灵：解析为游戏引擎的 UI 贴图或 DDS/TGA 图形资源。
- **命令与作用域上下文**
  - 本地化命令表达式中包含的作用域链（如 `[Root.GetName]`），使用本地化链接（`localisation_links`）和本地化命令字段（`localisation_commands`）进行语义解析与作用域推导。
- **本地化宏与插值**
  - 本地化参数 `$KEY$`：语义上可解析为对其他本地化条目的复用引用、封装变量引用（`$@var$`）或外层传入的宏参数。

### 2.3 存根与轻量元素边界

- **存根（Stub）**：支持文件存根（`ParadoxLocalisationFileStub`）、属性列表存根（`ParadoxLocalisationPropertyListStub`）、语言区域存根（`ParadoxLocalisationLocaleStub`）以及条目存根（`ParadoxLocalisationPropertyStub`）；这些存根以普通 `IElementType` 作为元素类型，通过 `LightLanguageStubDefinition` + `StubRegistryExtension` + `LightStubElementFactory` 注册，由 `LightStubBuilder` 构建，驱动高效的跨语言条目索引。
- **轻量元素（LightElement）**：本地化参数（`ParadoxLocalisationParameterLightElement`）与本地化片段（`ParadoxLocalisationSnippetLightElement`）作为轻量符号暴露给编辑器。

---

## 三、表格文件 (Paradox CSV)

### 3.1 文法级别 (Grammar Level)

Paradox CSV（`*.csv`）文法极简，完全为扁平行列结构：

- **CSV 文件** (`ParadoxCsvFile`)：文件顶层容器。
- **表头** (`ParadoxCsvHeader`)：文件的首行，包含一系列列名。
- **行** (`ParadoxCsvRow`)：数据记录行。
- **列** (`ParadoxCsvColumn`)：单元格数据节点，实现了 `ParadoxCsvExpressionElement` 与 `PsiLiteralValue`。

### 3.2 语义级别 (Semantic Level)

- **行规则与模式匹配** (`CwtRowConfig`)
  - CSV 没有语法上的复杂结构，其语义完全由 CWT 中的行规则驱动。
  - 匹配模式包括按键匹配（`key`，第一列为标识键）与按索引匹配（`index`，固定列位置映射）。
  - 支持配置表头跳过（`skip_last_row`、`skip_last_column`）。
- **动态复杂枚举声明**
  - 行规则的列配置可通过 `## declare_complex_enum = name` 将特定列的值提取并注册为复杂枚举项（`complexEnumValue`）。
- **表达式与引用接入**
  - `ParadoxCsvColumn` 接入 `ParadoxExpressionElement` 体系，使得单元格文本能够直接匹配并解析对定义、复杂枚举、变量或图片的引用。

### 3.3 存根与服务边界

- CSV 文件**不构建语法存根（Stub）**。表头与数据列的索引映射、列名称解析统一由 `ParadoxCsvPsiService` 在内存中按需动态构建。

---

## 四、规则文件 (CWT - Config World Tree)

### 4.1 文法级别 (Grammar Level)

CWT 是 Chronicle 的规则描述 DSL，文法上与 Script 接近，但语义上是纯元规则语言：

- **文件与根块**
  - CWT 文件 (`CwtFile`)：规则文件顶层容器。
  - 根块 (`CwtRootBlock`)：顶层无边界容器。
- **规则成员** (`CwtMember` / `CwtStatement`)
  - 属性规则 (`CwtProperty`)：键值对，包含属性键（`CwtPropertyKey`）与属性值（`CwtValue`）。
  - 值规则 (`CwtValue`)：布尔、数字、字符串、代码块（`CwtBlock`）。
- **三级注释体系**
  - 普通行注释（`#`）：纯文本注释。
  - 选项注释 (`CwtOptionComment`，`##`)：结构化选项元数据，内部包含选项键值对 (`CwtOption` / `CwtValue`)。
  - 文档注释 (`CwtDocComment`，`###` / `####`)：为规则项提供说明，支持 Markdown 渲染。

### 4.2 语义级别 (Semantic Level)

- **规则内存模型** (`CwtConfigGroup`)
  - CWT PSI 被加载后直接编译为常驻内存的只读数据模型 `CwtConfigGroup`。
  - 核心集合包括：类型声明（`types`）、声明规则（`declarations`）、行规则（`rows`）、作用域模型（`scopes` / `scope_groups`）、链接模型（`links`）、别名规则（`aliases` / `single_aliases`）、枚举与复杂枚举（`enums` / `complex_enums`）、宏规则（`macros`）等。
- **选项元数据** (`CwtOptionMetadata`)
  - 选项注释（`##`）在语义层被编译为结构化的选项元数据，包括基数约束（`cardinality`）、严重级别（`severity`）、作用域压栈（`push_scope`）、作用域替换（`replace_scopes`）等。
- **规则符号与轻量元素**
  - CWT 规则本身不是游戏数据，但当用户在编辑器中查询规则来源或查看规则导航时，由 `CwtConfigSymbolLightElement` 与 `CwtMemberConfigLightElement` 提供只读符号代理。

### 4.3 存根与独立性边界

- CWT **不进入游戏存根索引**。规则文件由 `CwtConfigGroupProcessor` 读取并常驻在规则分组缓存中，不作为游戏项目的物理代码索引。

---

## 五、跨语言共性与特例抽象

### 5.1 跨语言共性特征

1. **共享基础类型**：Script、Localisation 与 CSV 统一继承 `ParadoxLanguage`（基于 `ParadoxBaseLanguage`）和 `ParadoxFile`，共享统一的游戏类型与根目录识别逻辑。
2. **表达式抽象统一**：Script、Localisation 与 CSV 的文本单元均实现 `ParadoxExpressionElement`，使得通用引用解析、高亮与快速文档能够跨语言无缝协同。
3. **引号模型统一**：所有支持字符串引号的节点均实现 `PsiQuoteAwareElement`，将引号语义委托给平台统一的 `QuotePattern` 策略引擎。

### 5.2 核心特例与架构隔离

1. **CWT 架构独立**：CWT 直接继承 IntelliJ `Language`，不进入 `ParadoxLanguage` 基座，完全脱离游戏定义体系，作为只读的元模式层（Schema Layer）存在。详细原因见 [`psi-architecture.md`](psi-architecture.md)。
2. **Localisation 容器隔离**：本地化语言由于富文本的多样性，采用延迟解析的 `RichText` 容器模型，不参与 Script/CWT 的通用 `Member` / `Statement` 抽象。
3. **CSV 极简化**：CSV 仅抽象出 Header、Row 与 Column，无宏、无嵌套块、无成员层次，语义全部通过行规则映射。
4. **宏与参数的语言隔离**：Script 参数（`ParadoxScriptParameter`）与 Localisation 参数（`ParadoxLocalisationParameter`）在文法符号与转义边界上存在本质差异，两者在 PSI 层保持独立，仅在宏概念分类上保持对称。
