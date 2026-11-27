# 领域名词与术语表

> - 版本：`v3.1.0`
> - 更新日期：`2026-11-28`
> - 状态：**现行活规范（Living Specification）**
> - 文档定位：规范 Paradox Chronicle 插件代码实现、代码注释（KDoc）、UI 文本以及维护者文档中所使用的领域专有名词、推荐中文翻译、多载消歧与外部工具对照。
> - 维护原则：本文档客观记录现行代码、文档与 UI 中的标准译法及历史遗留差异，明确消歧边界，不作为单方面全局推进行政式改名的替代手段。

---

## 一、通用领域核心概念

| 英文原词 (English) | 推荐中文翻译 (Chinese) | 领域定义与上下文说明 |
| :--- | :--- | :--- |
| **scope** | **作用域** | 模组脚本中的上下文与“主语”，指示条件与效果的承载对象。 |
| **modifier** | **修正** | 游戏数值加成或减损属性，如静态修正、动态修正。 |
| **trigger** | **触发器** | 脚本中用于判定条件的谓语表达式。Stellaris 维基中常混用 condition。 |
| **effect** | **效果** | 脚本中用于执行具体状态变更的操作谓语。 |
| **scripted variable** | **封装变量** | 脚本中通过 `@var = 1` 声明的变量，分为本地封装变量与全局封装变量。 |
| **localisation** | **本地化** | 游戏多语言文本资源体系（YAML）。插件统一使用英式拼写。 |
| **define** | **定值** | 游戏引擎底层的全局数值常量或基础参数（位于 `common/defines/`）。 |
| **definition** | **定义** | 模组中由脚本声明的数据实体对象（事件、特质、国家理念等）。 |
| **scripted trigger** | **封装触发器** | 可复用的触发器宏组合。社区常译为“脚本化触发器”。 |
| **scripted effect** | **封装效果** | 可复用的效果宏组合。社区常译为“脚本化效果”。 |
| **script value** | **脚本值** | 可评估的复杂数值公式定义，支持动态参数传入。 |
| **game rule** | **游戏规则** | 游戏内部逻辑规则（如继承法、宣战合法性判定）。注意与 CWT 规则区分。 |
| **on action** | **动作触发** | 游戏引擎事件驱动分发点（如战争爆发、领导人死亡时调用的钩子）。 |
| **event** | **事件** | 弹出的交互式对话框或后台静默触发的逻辑单元。 |
| **event namespace** | **事件命名空间** | 事件 ID 前缀的命名空间声明（如 `country_event = { id = my_event.1 }`）。 |
| **sprite** | **精灵** | 界面贴图与图形元素引用，对应 `.gfx` 声明中的贴图条目。 |
| **macro** | **宏** | 语法或语义级别的代码复用结构，详见语法宏与语义宏。 |
| **inline script** | **内联脚本** | Stellaris 等游戏中通过文件级内联展开实现的语义宏（`inline_script`）。 |
| **definition injection** | **定义注入** | VIC3 / EU5 等游戏中向既有定义追加或替换字段的语义宏。 |
| **config (CWT)** | **规则** | CWT 规则文件体系中的规则声明与模式描述。 |
| **config group** | **规则分组** | 绑定到特定游戏类型或上下文的一组 CWT 规则集合。 |
| **game type** | **游戏类型** | 插件支持的具体游戏标识（如 `stellaris`、`ck3`、`vic3`、`eu4`、`eu5`）。 |
| **game version** | **游戏版本** | 游戏主程序与基础数据的语义化版本号。 |
| **game directory** | **游戏目录** | 游戏本体的本地安装根路径。 |
| **mod** | **模组** | 社区为游戏开发的扩展包或修改包。 |
| **mod descriptor** | **模组描述符** | 模组的元数据描述文件（`descriptor.mod`）。 |
| **mod source** | **模组来源** | 模组的物理存储来源，区分为本地目录、Steam 创意工坊与 Paradox Mods。 |
| **root info** | **根信息（游戏或模组信息）** | 插件内部用于描述游戏安装目录或模组根目录元数据的 `ParadoxRootInfo`。 |
| **file info** | **文件信息** | 插件内部用于描述特定文件在游戏或模组目录层级中相对位置的 `ParadoxFileInfo`。 |
| **file group** | **文件分组** | 用于根据目录特征（如 `common`、`events`、`localisation`）对文件进行归类的枚举 `ParadoxFileGroup`。 |
| **entry** | **入口（目录）** | 游戏或模组的基准目录层级（不一定是游戏或模组根目录）。可以存在多个入口，并分为主要入口和次要入口。|
| **file path** | **文件路径** | 文件在游戏或模组的目录层级中的常用路径表示（`ParadoxPath`）。相对于入口目录，并加入规则匹配。 |
| **member path** | **成员路径** | 脚本成员在脚本文件内部嵌套层级中的路径表示（`ParadoxMemberPath`）。 |
| **event target** | **事件目标** | 脚本中保存并跨上下文引用的动态对象目标。 |
| **variable** | **变量** | 作用域变量，存储在特定游戏作用域对象上的动态数值或标志。区别于 `@var` 封装变量。 |
| **flag** | **标志** | 存储在国家、星球或角色上的布尔型标记，属于动态值的一种。 |
| **scope type** | **作用域类型** | 具体的对象类别（如 `country`、`planet`、`character`）。 |
| **scope group** | **作用域分组** | CWT 规则中声明的一组作用域集合（如 `[country, federation]`）。 |
| **system scope** | **系统作用域** | 引擎预定义的特殊指针（`This`、`Root`、`Prev`、`From`）。 |
| **scope context** | **作用域上下文** | 记录当前节点所持有的作用域及系统作用域栈状态的内存结构（`ParadoxScopeContext`）。 |
| **scope switch** | **作用域切换** | 脚本中使上下文从一个对象转移到另一对象的操作。 |
| **scope link** | **作用域链接** | 复杂表达式中连接不同作用域或值字段的单步跳转节点。 |
| **union scope** | **并集作用域** | 插件内基于单父继承图模拟的复合作用域匹配机制。 |
| **modifier category** | **修正分类** | 规则中对修正项适用对象的归类（如适用国家、舰队或领袖）。 |
| **define namespace** | **定值命名空间** | 定值配置中的分类命名空间块（如 `NCharacter`、`NDiplomacy`）。 |
| **define variable** | **定值变量** | 定值命名空间下的特定参数键名及其对应的数值。 |
| **local scripted variable** | **局部封装变量** | 仅在当前脚本文件内部有效并覆盖的封装变量。 |
| **global scripted variable** | **全局封装变量** | 位于 `common/scripted_variables` 目录下、跨文件全局生效的封装变量。 |
| **synced localisation** | **同步本地化** | 不区分语言环境、跨语言共用的本地化定义（`localisation_synced`）。 |
| **inline localisation** | **内联本地化** | 在规则中声明的内联本地化引用（`localisation_inline`）。 |
| **load order** | **加载顺序** | 游戏引擎读取游戏本体与各模组文件的优先级顺序。 |
| **file overwrite / override** | **文件覆盖 / 覆盖** | 后加载的文件完全替换或重载先加载文件内容的机制。 |
| **definition source** | **定义来源** | 定义对象的物理生成形式，包括文件、属性、内联脚本与定义注入。 |
| **inline definition** | **内联定义** | 通过特定宏机制或块内部属性派生出的非独立声明定义。 |
| **declaration** | **声明** | CWT 规则中用来描述定义内部合法结构模式的规则定义。 |
| **usage** | **用法** | 宏、参数或定义在物理文件中的具体调用位置。 |
| **type key** | **类型键** | CWT 类型规则用来匹配定义的特征键。等同于对应的脚本属性的名字，但不一定是定义的名字。|
| **root key** | **顶级键** | CWT 类型规则用来匹配定义的特征键。等同于上钻得到的所有脚本属性的名字，可以存在多个和多组。 |
| **related localisation** | **相关本地化** | 与当前定义存在推导或生成关系的本地化条目。 |
| **related image** | **相关图片** | 与当前定义存在关联匹配关系的图标或贴图资源。可以指向特定类型的定义或图片文件。 |

---

## 二、脚本与本地化语言构造

### 2.1 脚本语法与语义构造

| 英文原词 (English) | 推荐中文翻译 (Chinese) | 领域定义与上下文说明 |
| :--- | :--- | :--- |
| **property** | **属性** | 由键与值通过分隔符构成的语法项。规范要求使用 property，避免使用 attribute。 |
| **value** | **值** | 属性的值部分，或直接出现在块中的字面量、块与数学节点。 |
| **direct value** | **直接的值** | 直接出现在代码块中作为列表项的值，而非作为属性的值。旧称“单独的值”。 |
| **block** | **块** | 由大括号 `{}` 包裹的语句或成员容器。 |
| **member** | **成员** | 属性（Property）与值（Value）的统称。 |
| **statement** | **语句** | 包含了成员、封装变量声明与条件化块的高层 AST 节点统称。 |
| **expression** | **表达式** | 脚本中承载文本、标识符或复杂逻辑的字面量单元。 |
| **complex expression** | **复杂表达式** | 运行期由 CWT 数据类型驱动解析出的轻量语法树结构。 |
| **parameter** | **参数** | 宏体系中的形参占位符，形式如 `$PARAM$`。 |
| **argument** | **传入参数（实参）** | 调用宏时传入的具体实参赋值（如 `MY_PARAM = "value"`）。 |
| **conditional block** | **条件化块** | 语法如 `[[PARAM] ...]`。 |
| **inline conditional block** | **内联条件化块** | 作为内联插值嵌入在字符串或标识符内部的条件化块。 |
| **conditional expression** | **条件化表达式** | 条件化块内部的条件判定部分。 |
| **conditional parameter** | **条件化参数** | 驱动条件化块是否展开的布尔参数。 |
| **interpolation** | **插值** | 嵌入在标识符或字符串内部的内联语法宏。 |
| **inline math** | **内联数学** | 脚本中的动态数学评估语法（`@[ ... ]`）。 |
| **syntax macro** | **语法宏** | 文法层可确认的宏构造，包括参数、条件化块、内联数学与封装变量引用。 |
| **semantic macro** | **语义宏** | 语义层由规则驱动展开的宏构造，包括内联脚本与定义注入。 |
| **script value reference** | **脚本值引用** | 带有参数传入管道的脚本值调用结构（`$script_value_reference`）。 |
| **define reference** | **定值引用** | 对游戏底层常量的调用结构（`Namespace\|Name`）。 |
| **array define reference** | **数组定值引用** | 对游戏底层常量数组项的调用结构（`Namespace\|Name\|index`）。 |
| **tags expression** | **标签集表达式** | 逗号分隔的标签序列，支持 `not(tag)` 取反。旧称 `dynamicValueSetExpression`。 |
| **tag type** | **标签类型** | 标签的预定义分类或前缀类型。 |
| **dynamic value** | **动态值** | 运行期产生的变量、事件目标或标志等统一动态数据体系。 |
| **dynamic value type** | **动态值类型** | 动态值的分类标识（如 `variable`、`event_target`、`flag`）。 |
| **complex enum** | **复杂枚举** | 由规则在脚本特定结构中扫描提取的动态枚举集合。 |
| **complex enum value** | **复杂枚举值** | 复杂枚举集合中的具体枚举项。 |
| **enum value** | **枚举值** | CWT 规则静态声明的枚举集合项。 |
| **snippet** | **片段** | 引用表达式中被剥离固定前后缀后的核心名字片段。 |
| **definition reference snippet** | **定义引用片段** | 针对定义引用的局部片段匹配。 |
| **localisation reference snippet** | **本地化引用片段** | 针对本地化引用的局部片段匹配。 |

### 2.2 本地化语言构造

| 英文原词 (English) | 推荐中文翻译 (Chinese) | 领域定义与上下文说明 |
| :--- | :--- | :--- |
| **localisation property** | **本地化属性** | 本地化 YAML 文件中的键值行。 |
| **localisation text** | **本地化文本** | 面向最终用户展示的本地化字面量内容。 |
| **locale** | **语言环境（语言区域）** | 本地化语言标记（如 `l_english`、`l_simp_chinese`）。 |
| **localisation command** | **本地化命令** | 本地化富文本中的 `[...]` 动态求值命令。 |
| **localisation command field** | **本地化命令字段** | 本地化命令链末尾调用的方法字段（如 `GetName`）。 |
| **localisation promotion** | **本地化提升** | 通过特定链接切换作用域后可继续调用的命令字段提升规则。 |
| **localisation link** | **本地化链接** | 驱动本地化命令中作用域切换的链接规则。 |
| **rich text** | **富文本** | 包含颜色代码、图标、命令与宏插值的本地化文本格式。 |
| **text format** | **文本格式** | 用于控制文本排版风格的自定义格式标记。 |
| **text icon** | **文本图标** | 嵌入在富文本中的单字符或内联小图标。 |
| **icon** | **图标** | 本地化文本中通过 `£...£` 引用的贴图精灵。 |
| **concept** | **概念** | 游戏内的词条概念，悬停可展示说明卡片。 |
| **grammatical tag** | **语法标签** | 用于根据性、数、格控制本地化文本变形的语法标签。 |
| **context tag** | **上下文标签** | 随执行环境传递的本地化条件标签。 |
| **escape sequence** | **转义序列** | 如 `\"`、`\n` 等转义字符。 |
| **tagged parameter** | **标签化参数** | 形式如 `<PARAM>` 的带标签参数，待与普通参数统一。 |

---

## 三、CWT 规则与语义系统术语

| 英文原词 (English) | 推荐中文翻译 (Chinese) | 领域定义与上下文说明 |
| :--- | :--- | :--- |
| **config file** | **规则文件** | 编写 CWT 规则的 `*.cwt` 文件。 |
| **config system** | **规则系统** | 插件内部由 CWT 驱动的代码智能核心子系统。 |
| **type config** | **类型规则** | 声明某种定义识别逻辑的规则（`CwtTypeConfig`，如 `type[...] = { ... }`）。 |
| **declaration config** | **声明规则** | 描述定义内部合法结构模式的规则项（`CwtDeclarationConfig`）。 |
| **row config** | **行规则** | 驱动 CSV 表格结构解析的规则模型（`CwtRowConfig`）。 |
| **link config** | **链接规则** | 驱动复杂表达式中作用域或值字段跳转的链接模型（`CwtLinkConfig`）。 |
| **scope config** | **作用域规则** | 声明游戏作用域类型及其关系的规则（`CwtScopeConfig`）。 |
| **scope group config** | **作用域分组规则** | 声明作用域集合约束的规则（`CwtScopeGroupConfig`）。 |
| **system scope config** | **系统作用域规则** | 声明系统预定义作用域原型的规则（`CwtSystemScopeConfig`）。 |
| **alias config** | **别名规则** | 声明可复用规则片段或扩展语法的规则（`CwtAliasConfig`，如 `alias[trigger:...]`）。 |
| **single alias config** | **单别名规则** | 声明扁平化单一结构的别名规则（`CwtSingleAliasConfig`）。 |
| **macro config** | **宏规则** | 描述语义宏模式与参数的规则（`CwtMacroConfig`）。 |
| **dynamic value type config** | **动态值类型规则** | 声明动态值有效作用域与合法性的规则。（`CwtDynamicValueTypeConfig`） |
| **complex enum config** | **复杂枚举规则** | 声明复杂枚举扫描路径与键值的规则（`CwtComplexEnumConfig`）。 |
| **database object type config**| **数据库对象类型规则**| 声明数据库对象表达式支持的类型及其映射规则（`CwtDatabaseObjectTypeConfig`）。 |
| **option metadata** | **选项元数据** | CWT 注释（`##`）解析生成的控制参数（`CwtOptionMetadata`）。 |
| **cardinality** | **基数约束** | 属性或值在块内出现次数的上下限约束（如 `## cardinality = 0..1`）。 |
| **severity** | **严重级别** | 规则校验失败时报告问题的严重级别（`warning`、`error` 等）。 |
| **push scope** | **作用域压栈** | 切换进入当前块内部的作用域类型（如 `## push_scope = country`）。 |
| **replace scopes** | **作用域替换** | 替换上下文映射槽的规则选项（如 `## replace_scopes = { ... }`）。 |
| **scope constraint** | **作用域约束** | 触发器或效果所要求的输入作用域范围。 |
| **data expression** | **数据表达式** | 规则属性键或值中的类型描述符（如 `filepath`、`scope_field`）。 |
| **data type** | **数据类型** | CWT 数据表达式解析出的类型枚举（`CwtDataType`）。 |
| **config resolution** | **规则解析** | 为特定 PSI 元素查找并绑定匹配 CWT 规则的过程。 |
| **config context** | **规则上下文** | 记录当前元素所处环境的规则树容器（`CwtConfigContext`）。 |
| **inferred config** | **推断规则** | 从使用处反向评估生成的非静态规则。 |
| **mock config** | **模拟规则** | 为动态构造的节点生成的轻量只读虚拟规则对象。 |
| **config expansion** | **规则展开** | 将别名规则等高级语法展开为实际候选集合的处理过程。 |

---

## 四、高风险多载与跨源差异消歧

为避免在跨子系统设计交流中产生歧义，以下高风险术语需遵循明确的消歧规则：

### 4.1 `define`（定值）与 `definition`（定义）

- **`define`（定值）**：专指位于 `common/defines/` 目录下由引擎导出的底层浮点、整数或布尔参数。代码中对应 `ParadoxDefineManager`、`ParadoxDefineVariableInfo`。
- **`definition`（定义）**：指模组创作者在脚本中声明的具有具体类型和名称的数据对象（如 `civic = { ... }`）。代码中对应 `ParadoxDefinitionManager`、`ParadoxDefinitionInfo`。
- **跨源差异消歧**：社区中文翻译常将 `defines` 翻译为“定义”；在插件内部必须严格使用“定值”与“定义”进行区分。

### 4.2 `definition` 的跨源模型对照

| 来源体系 | 对应术语 | 抽象层级说明 |
| :--- | :--- | :--- |
| **Paradox Chronicle** | **`definition`（定义）** | 统一将所有具备类型键和数据块的对象抽象为定义对象。 |
| **Paradox 官方 Wiki** | **`game object` / `entry` / `database entry`** | 官方通常根据具体领域分别称呼条目或数据库对象。 |
| **CWTools** | **`type` / `entity`** | CWTools 规则体系侧重于将其视作某种 `type` 的实例。 |
| **Tiger (ck3/vic3-tiger)**| **`item`** | Tiger 将脚本中的顶层数据块抽象为待检查的 item。 |

### 4.3 `variable`（变量）与 `scripted variable`（封装变量）

- **`variable`（变量）**：游戏作用域对象上运行时保存的动态变量（通过 `set_variable` 设置，通过 `var:x` 访问）。
- **`scripted variable`（封装变量）**：静态源码中通过 `@my_var = 1` 声明并静态替换的数值常量。代码中对应 `ParadoxScriptScriptedVariable`。

### 4.4 `config`（规则）与 `game rule`（游戏规则）

- **`config`（规则）**：CWT 体系中的语法与语义匹配规则。在插件内部单提“规则”默认指 CWT 规则。
- **`game rule`（游戏规则）**：游戏引擎逻辑中的特定规则对象（如 `game_rules/` 目录下的设定）。在文档中必须显式带上“游戏”二字以示区分。

### 4.5 `injection` 的多重语义消除

- **定义注入（Definition Injection）**：VIC3/EU5 中的一种语义宏（`inject:xxx`），属于模组脚本业务逻辑范畴。
- **语言注入（Language Injection）**：IntelliJ 平台能力，指在字符串或参数内建立子语言 PSI（如参数值脚本注入），属于 IDE 基础设施范畴。
- **代码注入（Bytecode / Class Injection）**：插件底层的 `icu.windea.pls.inject` 子系统，通过修改平台字节码进行底层修复。

### 4.6 `event target` 术语统一

- **事实依据**：早期文档与个别 KDoc 曾交替使用“事件目标”与“事件对象引用”。最新代码提交（`dc4ddf0aaa`）已全面统一为 **`event target` -> 事件目标**，相关引用统称为“事件目标引用”。

---

## 五、常用 IntelliJ 平台工程术语

| 平台原词 | 推荐中文 | 插件实现与场景说明 |
| :--- | :--- | :--- |
| **PSI** | **程序结构接口** | Program Structure Interface，平台的核心语法与语义抽象层。 |
| **AST** | **抽象语法树** | Abstract Syntax Tree，由解析器生成的物理语法树。 |
| **Stub** | **存根** | 跨文件索引的二进制快速序列化载体。插件采用基于 `StubRegistryExtension` + `LightStubElementFactory` 的现代 Light Stub 体系，元素类型保持普通 `IElementType`，不注册 `IStubElementType`。 |
| **LightElement** | **轻量元素** | 平台中不具备物理 AST 节点对应物的虚拟语义符号抽象。 |
| **Language Injection** | **语言注入** | 平台将一种语言嵌入到另一种语言宿主内部的机制。 |
| **ModificationTracker** | **修改追踪器** | 驱动 PSI 缓存失效的版本自增标记。 |
| **VirtualFileWindow** | **虚拟文件窗口** | 平台分配给语言注入代码片段的内存虚拟文件代理。 |
| **Extension Point (EP)** | **扩展点** | 插件架构中用于解耦与动态装配组件的核心机制。 |
| **Inspection** | **代码检查** | 静态扫描代码潜在缺陷与错误的后台检查器。 |
| **Intention (Action)** | **意向（意向动作）** | 用户在特定光标位置按 Alt+Enter 触发的上下文快捷重构。最新 UI 文案已统一为“意向”。 |
| **Quick Fix** | **快速修复** | 代码检查上报错误后附带的一键修复动作。 |
| **Folding** | **代码折叠** | 折叠多行代码块、长文本或内联宏的编辑器功能。 |
| **Inlay Hint** | **内嵌提示** | 在代码行内穿插展示的虚线框提示（如作用域信息、颜色方块）。 |
| **Annotator** | **语法标注器** | 负责为 PSI 元素施加自定义文本属性与高亮样式的组件。 |
| **Line Marker** | **装订线标记** | 编辑器左侧行号旁的图标标记，用于指示重载、导航或图片预览。 |
| **Line Marker Provider** | **装订线提供者** | 收集并为文件生成装订线标记的扩展组件。 |
| **Reference Provider** | **引用提供者** | 为特定 PSI 节点注入跨文件跳转引用的贡献者组件。 |
| **Search Scope** | **搜索作用域** | 平台范围内的文件检索过滤器（`GlobalSearchScope`）。注意与领域作用域区分。 |
