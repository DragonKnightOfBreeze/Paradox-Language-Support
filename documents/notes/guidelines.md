# 开发规范

## 代码架构

### 模块

顶级包名 `icu.windea.pls` 下的子包各自对应一个模块。

关于模块：

- `icu.windea.pls.core` - 通用基础设施。包括标准库、平台或第三方库的通用扩展、工具和组件。
- `icu.windea.pls.base` - 插件自身和平台集成的基础设施。包括内部状态处理、外部数据处理、环境检测、高层平台集成以及其他相关代码。
- `icu.windea.pls.cwt` - CWT 语言支持的基础设施。通常是语义无关的。
- `icu.windea.pls.script` - 脚本语言支持的基础设施。通常是语义无关的。
- `icu.windea.pls.localisation` - 本地化语言支持的基础设施。通常是语义无关的。
- `icu.windea.pls.csv` - CSV 文件语言支持的基础设施。通常是语义无关的。
- `icu.windea.pls.images` - 图片支持的基础设施。包括 SPI、编辑器、动作、渲染、元数据以及其他相关代码。
- `icu.windea.pls.config` - 规则系统的基础设施。包括规则、规则表达式、规则分组以及其他相关代码。
- `icu.windea.pls.lang` - 语义模块。包括语义匹配、语义解析、语言构造支持、语言功能支持、领域特定逻辑以及其他相关代码。
  - `match` - 语义匹配（规则、引用解析、索引、特定策略等。
  - `resolve` - 语义解析（规则、引用解析、索引、特定策略等）。
  - `util` - 高级管理器（门面、可能经过缓存）和特殊组件（评估器、渲染器等）。
- `icu.windea.pls.extensions` - 扩展模块。提供对第三方插件的集成和扩展。
  - `diagram` - 用于官方 Diagrams 插件。提供额外的 diagramProvider 和 diagramSettings。
  - `json` - 用于官方 JSON 插件。提供额外的 jsonSchemaProvider。
  - `markdown` - 用于官方的 Markdown 插件。提供额外的解析逻辑和代码检查。
- `icu.windea.pls.integrations` - 集成模块。提供对第三方工具的集成。
  - `images` - 用于外部图片工具（[texconv](https://github.com/microsoft/DirectXTex/wiki/Texconv) 和 [magick](https://www.imagemagick.org)）。
  - `lints` - 用于外部检查工具（[tiger](https://github.com/amtep/tiger)）。驱动相关的外部注解器和代码检查。
  - `translation` - 用于翻译工具（[Translation Plugin](https://github.com/yiiguxing/TranslationPlugin)）。驱动相关的意向和动作。
- `icu.windea.pls.tools` - 工具模块。提供一些附带的实用工具和集成。例如游戏启动器和规则生成器。
- `icu.windea.pls.ep` - 用于提供语言构造支持、语言功能支持以及部分便捷功能的多种 EP 接口和实现。

### 模块依赖

> - [ ] `icu.windea.pls.ep.util.data` and `icu.windea.pls.ep.util.presentation` dependencies in `icu.windea.pls.extensions.diagram.provider`

关于模块之间的依赖关系：

- `core` 模块应尽可能避免依赖其他任何模块（尤其是 `lang` 模块），并且总是可以被其他任何模块依赖。
- `base` 模块可以在必要时依赖于其他任何模块，并且可以在必要时被其他任何模块依赖。
  - 因为其包含了各类跨模块、跨语言的基础设施代码，例如插件设置相关的代码。
  - 考虑到插件首先就是作为一个 IDEA 插件而存在的，目前不将“插件特定的基础设施”与“平台特定的基础设施”划分到不同的模块中，如同 `core` 模块的做法一样。
- `cwt` `script` `localisation` `csv` 这些语言基础设施模块应尽可能避免依赖其他任何语言基础设施模块，以及 `lang` 模块。除了使用跨语言且领域特定的接口，以及其他少数特殊情况。
  - 未来可能考虑将这些跨语言且领域特定的接口，移到 `base` 模块或者某个新增模块。
  - 即使如此，部分双向依赖仍然是难以避免，或者就是设计如此的。
- `images` 模块不应依赖于任何语言基础设施模块，以及 `configs` `lang` `tools` 模块。
- `config` 模块不应依赖于除了 `cwt` 以外的任何语言基础设施模块，以及 `images` `lang` `tools` 模块。
- `lang` 模块不应依赖于 `tools` 模块，因为后者被期望是语义无关的（尽管应当领域相关），或者仅由后者依赖前者。
- `integrations` 模块可以并且期望在必要时依赖于各个语言基础设施模块，以及 `images` `configs` `lang` 模块。
- `extensions` 模块可以并且期望在必要时依赖于各个语言基础设施模块，以及 `images` `configs` `lang` 模块。
- `tools` 模块可以在必要时依赖其他任何模块，但不应被其他任何模块依赖（除了 `base` 模块）。
- `ep` 模块应尽可能地仅被 `config` `lang` `tools` 模块依赖。它们不应被 `integrations` `extensions` 模块依赖（需要后续重构以完全达成）。

虽然期望尽可能移除模块之间的非预期的双向依赖，有时也存在导入语句仅用于文档注释的情况……

### 备注

关于 `icu.windea.pls.config`：

- 这里用于存放规则系统的基础设施，但并非与规则系统紧密相关的全部代码。这意味着它们通常不需要依赖游戏或模组文件。
- 诸如规则上下文、规则匹配、模板解析、模板匹配等逻辑，由于发生在语义层，相关代码通常位于 `lang.match` 和 `lang.resolve` 中。

关于 `icu.windea.pls.lang.match` 和 `icu.windea.pls.lang.resolve`：

- 这里用于存放领域特定的、语义级别的匹配和解析逻辑。
- 这里的代码相对 `icu.windea.pls.lang.util.*Manager` 中的更加底层，后者仅在必要时提取和重构到前者中，大部分仍然可以保留。
- 尽可能仅在这里（一般是在 `*Service` 中）调用涉及匹配和解析逻辑的 EP，而不是在 EP 接口的伴生对象或 `*Manager` 中。
- 为了优化编制和查询索引时的性能，有时需要直接传入 LighterAST，而非 PSI。
- 主要基于索引、引用解析、规则以及特定策略。

关于 `icu.windea.pls.config.manipulation` 和 `icu.windea.pls.lang.manipulation`：

- 这里用于存放强调“操作/manipulate”的服务，以及相关的模型类、枚举、扩展方法等。
- 这里的代码通常会转换目标（规则对象、PSI）的状态，或者直接修改为需要的状态。
- 可以发生在多个不同的阶段和级别（例如，构建解析上下文时、进行语义解析时、调用语言功能时）。
- 同时作为部分语义解析逻辑，以及部分代码重构、意向动作和快速修复的基础设施。
- 常见的“操作”包括：“转换/transform”、“内联/inline”、“合并/merge”，等等。

关于 `Service` `Manager` 和 `Util`/`Factory`/`Builder`：

- `Service` - 更加底层的服务，可以包含领域的分析、匹配或解析逻辑，这些逻辑可能需要交给具体的扩展点实现。
- `Manager` - 更加高层的管理器，可以包含各种可以直接使用的领域方法，这些方法可能需要依赖对应的服务方法。
- `Util`/`Factory`/`Builder` - 拥有特定用途的工具类，其中的方法可能仅适用于某种特定组件或功能，或者只在少数特定场合才会用到。
- `Service` 和 `Manager` 不一定必须声明为 IntelliJ 服务，如果是无状态的，它们也可以直接声明为 Kotlin 单例对象。
- 缓存逻辑一般位于 `Manager` 级别，`Service` 级别一般是直接的获取和解析逻辑，不经过缓存。

## 代码规范

### 命名

原则：

- 对于类名，如果需要，优先使用语言和领域的对应前缀，如 `Cwt...` `Paradox...` `ParadoxScript`。
- 对于动作（actions）和意向（intentions），优先使用动词形式（如 `CopyDefinitionNameIntention`）。
- 优先使用基于单词或前缀的缩写（例如，对于 `scopeContext`，`context` `sc` `c` 都是可接受的，而 `ctx` 则是不建议的）。

### 导入

原则：

- 显式导入适用于绝大多数情况。
- 星号导入仅适用于少数特殊情况。例如，DSL 风格的包。
- 如果同包的显式导入超过上限（50），改为使用星号导入。
- 逻辑与结构优先于导入排序与清理；不因少量未排序或未使用的导入阻塞协作，但在使用 IDE 整理导入时应留意保留下述星号导入白名单。

优先使用星号导入的包名（包含子包）：

- `com.intellij.ui.dsl` - IntelliJ UI DSL
- `icu.windea.pls.test.dsl` - 测试 DSL
- `icu.windea.pls.lang.resolve.complexExpression.dsl` - 复杂表达式的 DSL
- `icu.windea.pls.lang.resolve.complexExpression.nodes` - 复杂表达式节点
- （待补充）

### 注释

原则：

- 对于 Java 和 Kotlin，文档注释默认用中文编写，除非存在特殊情况，或者需要与上下文对齐。
- 对于其他语言，文档注释默认用英文编写，除非存在特殊情况，或者需要与上下文对齐。
- 普通注释用中文或英文编写，取决于上下文。
- 对于 Kotlin 代码中的文档注释，应优先 Kotlin 的文档注释规范（KDoc），而非 Java 的。

约定：

- 如果必要，灵活地使用内联链接（如 `[PsiElement]`），嵌入到描述文本中，或者显式引用对应的代码位置。
- 如果必要，灵活地使用引用标签（如 `@see PsiElement`），显式引用对应的代码位置。
- 尽量用自然语言描述，避免引用具体的代码片段（如有必要，可以引用关键代码位置）。
- 尽量不要直接为成员属性编写文档注释，除非需要足够详细。它们应当一并放到所在的类或接口的文档注释中（使用 `@property`）。
- 尽量不要通过 `@param` 单独为方法参数编写文档注释，除非需要足够详细。它们应当尽可能地合并到对方法的描述和说明中，并在必要时通过 `[param]` 引用。

### 缓存

原则：

- 优先使用 `icu.windea.pls.core.util.CachesKt.CacheBuilder` 构建缓存。
- 优先使用 `com.github.benmanes.caffeine.cache.Interner` 去重字符串。
- `icu.windea.pls.core` 中的全局缓存，一般使用强引用的值，同时指定上限和超时。
- 规则对象这类可能数据量巨大的缓存，一般使用软引用的值。
- 与 IDE 生命周期相关的缓存，一般使用软引用的值。

### 索引

原则：

- 直接保存到文件级别的分析数据（如语言区域、文件路径等），应使用文件索引（`FileBasedIndex`）。
- 直接基于分析数据以及 PSI 的类型与结构的数据（如封装变量、本地化等），应优先使用存根索引（`StubIndex`）。
- 需要解析 PSI 引用，或者需要依赖规则数据的数据（如定义、复杂枚举等），应优先使用文件索引（`FileBasedIndex`）。
- 为了优化性能，对于文件索引，需要依赖解析得到的匹配的成员规则的数据，应考虑统一使用合并索引（`ParadoxMergedIndex`）。
- 为了优化性能，对于文件索引，应在必要时压缩写入的数据（如使用 `readOrReadFrom` 或 `readIndexedStringList`）。

### 编码指南

规范：
- 获取协程作用域：使用 `ChronicleFacade.getCoroutineScope(project)`（项目级别）或 `ChronicleFacade.getCoroutineScope()`（应用级别）。
- 获取规则分组：使用 `ChronicleFacade.getConfigGroup(project, gameType)`（项目级别）或 `ChronicleFacade.getConfigGroup`（应用级别）。
- 引入新的设置、选项、能力项：对应地使用 `...Settings`（平台服务）、`...Options`（只读数据类）以及 `ChronicleCapabilities`。
- 处理数据（Json/Markdown/等等）：统一使用 `JsonService`，`MarkdownService`，等等。

通用基础设施：
- 扩展方法：`icu.windea.pls.core` 中提供了许多适用于标准库、平台、集合、并发、UI 等的扩展方法。诸如 `orNull()` `castOrNull` `runCatchingCancellable()`。
- 组件与策略：`icu.windea.pls.core` 中提供了许多有用的组件与策略，以及对应的服务和扩展方法。诸如 `AntMatcher` `QuotePattern` `ProcessExecutor`。
- 工具：`icu.windea.pls.core.util` 中提供了许多有用的工具、服务和扩展方法。诸如 `Keys.kt` `KeyAccessors.kt` `Caches.kt` `SoftValue` `RecursionGuard`。

## 命名

### 文件与符号命名

| 类别 | 约定 | 示例 |
| :--- | :--- | :--- |
| 单类 / 单对象文件 | `PascalCase` | `ParadoxDefinitionManager.kt` |
| 包级辅助文件 | `_types.kt`、`_accessors.kt`、`_models.kt` | `_types.kt` |
| 类级辅助文件 | `SomeExtensions.kt`、`SomeAccessors.kt` | `ParadoxDefinitionExtensions.kt` |
| EP 分组实现文件 | `SomeProviders.kt`、`SomeProviders.Category.kt` | `ParadoxHintTextProviders.kt` |
| 类名 | `PascalCase`，按需加领域前缀（`Cwt`、`CwtConfig`、`Paradox`） | `ParadoxDefinitionInfo`、`CwtTypeConfig` |
| 抽象类后缀 | `Base` | `ParadoxScriptExpressionSupportBase` |
| Action / Intention / Inspection | 动词或形容词开头，不加领域前缀 | `CopyPathFromRootProvider`、`GoToPathAction` |
| 函数与变量 | `camelCase` | `getInfo`、`resolveInfo` |
| 常量 | `UPPER_SNAKE_CASE` | `EMPTY_OBJECT` |
| 工厂方法 | `PascalCase()` | `SomeFactoryMethod()` |
| 枚举值 | `PascalCase` 或 `UPPER_SNAKE_CASE`（无严格限定） | `Stellaris`、`GAME_TYPE` |

### 约定

如何命名拥有特定用途的接口：

- 可感知某一领域模型
  - `*Aware`
- 用于在语义级别解析特定领域模型（规则、表达式等）
  - `*Resolver`
- 用于提供仅适用于特定组件的工具方法（包括扩展方法）
  - `*Scope` - 限定作用域

如何命名拥有特定用途的数据类：

- 表示选项/设置
  - `*Options` - 不可变，可能提供默认值
  - `*Settings` - 可变
- 表示综合属性/继承属性（synthesized attribute/inherited attribute）
  - `*Attributes` - 通常不可变
- 表示上下文对象
  - `*Context` - 通常不可变

如何命名 EP 的实现类：

- 建议格式：`[Domain][Layer][ImplementationType][Role]`
  - `[Domain]` - 领域前缀，如 `Cwt` `Paradox` `Stellaris`
  - `[Layer]` - 分层限定，如 `Base` `Core` `Default`
  - `[ImplementationType]` - 具体的实现类型，暗示实现逻辑
  - `[Role]`  - 角色名，EP 接口名的核心部分，如 `ExpressionSupport`
  - 始终“领域优先”，确保自然分组
- 关于各种实现：
  - `Default`  - 默认实现，适用于唯一默认的场景，或者比后续实现更一般的场景
  - `Base`  - 基础实现，适用于存在内置的核心实现或扩展实现的场合
  - `Core` - 核心实现，同样内置，但是相比基础实现要更高级
- 抽象类：
  - 可以考虑把 `Base` 放到最后，如 `ParadoxScriptExpressionSupportBase`

领域相关：

- 对于 `key = value` 形式的语法：称之为 *属性/property*，而非 *attribute* 等。
- 对于 `value` 形式的语法：称之为 *值/value*。如果其直接位于某个容器（块/条件化块/顶级块等）中，称之为 *直接的值/directValue*。
- 对于 `$PARAM$` 形式的语法：称之为 *参数/parameter*，而非 *宏/macro* 等。
- 对于 `[[PARAM]text]` 形式的语法：称之为 *条件化块/conditional block*。
- 将 *封装变量引用/参数/条件化块* 等统一归类为 *语法宏/syntax macro*。
- 将 *内联脚本/定义注入* 等统一归类为 *语义宏/semantic macro*。

### 翻译

领域相关（常见词汇对照）：

- *config* → *规则*（特指 CWT 规则；反之亦然）
- *scope* → *作用域*
- *modifier* → *修正*
- *trigger* → *触发器*
- *effect* → *效果*
- *scripted* → *封装*（例如：*scripted variable* → *封装变量*、*scripted trigger* → *封装触发器*、*scripted effect* → *封装效果*）
- *script value* → *脚本值*
- *game rule* → *游戏规则*
- *define* → *定值*
- *definition* → *定义*
- *on action* → *动作触发*（推荐译法）
- *localisation* → *本地化*
- *event* → *事件*（*event namespace* → *事件命名空间*）
- *sprite* → *精灵*
- *macro* → *宏*（*inline script* → *内联脚本*、*definition injection* → *定义注入*）

### 选型

*计算/calculate* VS *评估/求值/evaluate*：

- 评估内联数学表达式（inlineMathExpression）：应当使用 *evaluate*，因为涉及到参数解析和上下文。
- 评估触发器（trigger）：使用 *evaluate*，这还需要将动态的游戏数据加入上下文。

*call* VS *invoke*：

- 核心意思完全相同，都是指“执行一段代码（如函数或方法）”。
- **call 偏向基础、直接的操作**：最常见于**静态调用**。即在编译阶段就确定了要执行哪个函数，也泛指任何普通的函数执行。
- **invoke 偏向间接、动态或特殊的执行**：常见于**反射**（通过名称调用未知方法）、**委托/回调**、**动态调用**（如 JavaScript 的 `apply`），以及**跨线程操作**（如在 UI 线程执行代码）等场景。
- 对于 scripted_trigger/scripted_effect/script_value/inline_script - 使用 *call*
- 对于 event - 使用 *invoke*（或 *trigger/fire*）

## 格式

### 代码格式（语法文件）

对于 `flex` 文件：

- 规则的名字应使用 snake_case。
- 规则的分隔符（`::=`）周围需要添加空格。
- 产生式中的一元运算符（如 `?` `*` `+`）之前不需要添加空格。
- 产生式中的二元运算符（如 `|`）周围需要添加空格。

对于 `bnf` 文件：

- 宏的名字应使用 PascalCase。避免与词元（token）和词法状态（lexical state）的名字混淆。
- 宏的分隔符（`=`）周围需要添加空格。

### 文本格式

关于代码检查的描述文本（即报错文本/问题描述文本）：

- 作为标识符（名字、类型等）的英文文本应优先使用反引号括起（而非单引号），除非在冒号之后（此时不需要用任何形式的引号括起）。
- 多个标识符之间应优先使用英文逗号隔开。

### 文档格式

整体上的原则：

- 提供中英文双语的自述文档（`README.md`）。
- 提供中英文双语的更新日志文档（`CHANGELOG.md`）。更新项的中英文双语描述的通常放到同一行。
- 提供中英文双语的参考文档（位于 `docs` 目录中）。
- 暂时仅提供英文的贡献指南文档（`CONTRIBUTING.md`）和贡献者文档（`CONTRIBUTORS.md`）。

关于 Markdown 排版与列表：

- 不要在句子中途为了控制每行字符长度而换行。
- 段落可以完全不换行，需要换行时绝大部分情况下只在冒号、分号或句号之后换行（适用于中文、英文及其他任何语言）。
- 多行列表等列举中每项结尾应使用同样的符号（通常统一用句号，或统一省略结尾符号），不要前面几项用分号、最后一项用句号。
- 行内列举不受此限制，必要时可使用分号。

关于更新日志文档（`CHANGELOG.md`）：

- 提供更新项的中英文双语描述，除非是可以直接引用 Issue 标题等少数情况。
- 更新项文本的标准格式是 `[tag] [topic] 描述 / Description (issue) (commit) (contributor)`，除了描述部分之外都不是必须的。
- 更新项中的标签和话题通常使用小写，多个时用 `/` 隔开，并且放到英文方括号中。
- 更新项文本中的 Issue、提交、贡献者需要使用作为链接的普通文本（而非内联代码），并且放到英文括号中。

### 提交信息

整体上的原则：

- 不要求严格使用规范化的提交信息（例如，`fix: ...` `refactor: ...` `feat: ...`）。
- 提交信息可以使用中文或英文。
- 如果存在，提交信息中应尽量标出关键的相关文件位置/代码位置（以内联代码的形式）。
- 如果存在，提交信息中应标出相关的 Issue（在概述文本的结尾，以 `(#1)` 的形式）。
- 可以考虑在提交信息中标出相关的主题、领域、分类等（在概述文本的开始，以 `[fix]` `[refactor/match]` 的形式）。

## 排序

### 模块相关

- core - `icu.windea.pls.core`
- base - `icu.windea.pls.base`
- cwt - `icu.windea.pls.cwt`
- script - `icu.windea.pls.script`
- localisation - `icu.windea.pls.localisation`
- csv - `icu.windea.pls.csv`
- images - `icu.windea.pls.images`
- config - `icu.windea.pls.config`
- lang - `icu.windea.pls.lang`
- extensions - `icu.windea.pls.extensions`
- integrations - `icu.windea.pls.integrations`
- tools - `icu.windea.pls.tools`
- ai - `icu.windea.pls.ai`

### 语法相关

> TODO：尚未严格实施

### 语法相关

脚本语言：

- *属性/property*
- *值/value*
- *封装变量/scripted variable*
- *颜色/color*
- *内联数学/inline math* - 懒加载其中的内容。
- *参数/parameter* - 作为插值的其中一种形式，应放到末尾。
- *条件化块/conditional block* - 作为插值的其中一种形式，应放到末尾。
- *内联数学/inline math* 中的内容 - 懒加载。

本地化语言：

- *本地化列表/property list*
- *语言环境/locale*
- *属性/property*
- *属性键/property key*
- *属性值/property value*
- *属性值/property value* 中的内容 - 懒加载。

本地化语言，富文本：

- *普通文本/text*
- *彩色文本/colorful text* - TODO：可能需要考虑重命名为 *文本颜色/text color*。可能不受所有主要游戏类型支持，因此需要降低优先级。
- *参数/parameter*
- *命令/command*
- *概念命令/concept command* - 仅限 Stellaris。
- *图标/icon* - TODO：可能不受所有主要游戏类型支持，因此需要降低优先级。
- *文本图标/text icon* - 仅限基于 Jomini 的游戏类型。
- *文本格式/text format* - 仅限基于 Jomini 的游戏类型。

## 测试与基准测试

测试与微基准测试规范已按专题拆分至独立专册：

- **常规测试与平台集成测试**：参见 [测试指南](guidelines/testing.md)（`ChronicleTestScope`、测试 DSL、解析/高亮/回归/快照/受限测试）。
- **微基准测试（JMH）**：参见 [基准测试指南](guidelines/benchmarking.md)（`src/jmh/kotlin` 约定、防失真准则，以及 JMH 微基准与 IDEA Profiler 在“不能测量插件实际运行性能”上的定位边界）。

## UI

### 图标设计

参见：[图标设计.md](/documents/design/icon-design.md)

### 配色方案

参见：[配色方案.md](/documents/design/color-scheme.md)