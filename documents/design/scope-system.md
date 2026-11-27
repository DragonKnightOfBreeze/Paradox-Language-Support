# 作用域系统与上下文推导设计规范

> - 版本：`v3.1.0`
> - 更新日期：`2026-11-28`
> - 状态：**现行活规范（Living Specification）**
> - 文档定位：规范 Paradox Chronicle 中游戏作用域（Scope）分类模型、并集作用域模拟机制、CWT 规则侧驱动体系、`ParadoxScopeContext` 数据结构、上下文传播与合并管线、索引推断机制以及现行已知边界。
> - 互链说明：复杂表达式中的作用域字段与链式解析详见 [`complex-expressions.md`](complex-expressions.md)；文法层与语义层映射详见 [`language-abstractions.md`](language-abstractions.md)；底层 PSI 架构详见 [`psi-architecture.md`](psi-architecture.md)。

---

## 一、作用域分类与核心数据模型

### 1.1 作用域在 Paradox 脚本中的本质

作用域是 Paradox 游戏脚本语义中的**主语（Subject）**与执行上下文。脚本中的条件判定（Trigger）与效果执行（Effect）均在特定的作用域对象（如 `country`、`planet`、`character`、`system`）内部运行。

在插件体系中，作用域系统负责回答三个核心问题：
1. 当前代码块执行时，主语属于哪一种或哪几种作用域类型？
2. 当前链式表达式（如 `root.owner.capital_scope`）执行后，目标作用域将切换到什么类型？
3. 当前调用的触发器、效果或变量是否在当前作用域上下文的合法约束范围之内？

### 1.2 `ParadoxScope` 的封闭类型层次

插件使用密封接口 `ParadoxScope` 表达单一作用域类型，仅存在三种实现（位于 `icu.windea.pls.model.scope.ParadoxScope`）：

```text
ParadoxScope (sealed interface)
├─ Default(id: String, index: Int)   // 具体的规范化作用域类型（如 "country", "planet"）
├─ Any                               // 任意作用域哨兵（id = "any", index = -1, isUnsure = true）
└─ Unknown                           // 未知作用域哨兵（id = "?", index = -2, isUnsure = true）
```

- **ID 规范化**：所有作用域 ID 经 `ParadoxScopeResolver.getScopeId` 统一转换为全小写、下划线分隔，并经内存弱引用 Interning 规范化。`all` 关键字在公共解析路径中被归一化为 `any`。
- **快速索引**：每个 `Default` 作用域实例在初始化时分配全局自增的整型 `index`，以支持基于高密度哈希图（`Int2ObjectOpenHashMap`）的高性能位图与索引匹配。

### 1.3 系统作用域命名槽

除了常规对象作用域类型外，引擎提供了固定的系统级指针。插件通过系统作用域槽进行追踪：

- `This`：当前所在的作用域上下文。
- `Root`：当前执行链最外层的主作用域上下文。
- `Prev` / `PrevPrev` / `PrevPrevPrev` / `PrevPrevPrevPrev`：前置层级调用上下文栈。
- `From` / `FromFrom` / `FromFromFrom` / `FromFromFromFrom`：事件分发或动作触发的来源上下文。

### 1.4 并集作用域（Union Scopes）的模拟机制

在 Stellaris 等游戏中，存在如 `carrier`（航空母舰）等复合概念，既可以指代 `Ship` 也可以指代 `Planet`。在插件内部：

1. **非一等公民类型**：插件在类型系统上**没有** `ParadoxScope.Union` 实体类，每个上下文节点在物理上仍然只持有一个单一的 `ParadoxScope`。
2. **基于继承图的并集模拟**：
   - 规则文件（`scopes.cwt`）中通过 `is_subscope_of = carrier` 声明单父继承关系（如 `planet` 与 `ship` 声明其父为 `carrier`）。
   - `CwtScopeModel` 将别名、父作用域、子作用域预编译为五类索引映射（`base2Aliases`、`base2ParentScopes`、`base2ChildScopes`、`base2MatchedScopes`、`base2PromotedScopes`）。
3. **匹配与合并服务机制**：
   - **匹配阶段 (`ParadoxScopeMatchService`)**：采用宽松的双向匹配策略。父作用域可以匹配其子作用域，子作用域同样可以匹配其父作用域。
   - **合并阶段 (`ParadoxScopeMergeService`)**：采用向上提升策略。若合并分支包含子作用域与父作用域，提升为父作用域；若合并多个兄弟子作用域，尝试提升为它们的共同父作用域。

### 1.5 `Any`、`Unknown` 与 `null` 的非对称匹配契约

匹配引擎在处理缺省或通配状态时，遵循严格的非对称契约（设计事实依据 `ParadoxScopeMatchServiceTest`）：

- **`null`**：在匹配中视为“全量透传/总是匹配”；在合并中视为“缺省”，直接取另一非空项。
- **`Any`**：作为当前输入时匹配任意期望作用域；作为规则期望（`scopeToMatch`）时被视为全通配符。
- **`Unknown` (`?`)**：作为当前输入时匹配任意期望（宽容解析失败）；但**作为规则期望时不是通配符**，只有当输入同样为 `Unknown` 或具体 ID 完全相同时才匹配成功。
- **合并优先级**：`Any` 优先于 `Unknown`，`Unknown` 优先于具体作用域。两者同时出现时，合并结果为 `Any`。

---

## 二、CWT 规则侧驱动体系

作用域系统的合法性校验与推导完全由 CWT 规则文件体系驱动：

### 2.1 规则文件分类与模型映射

| 规则源文件 | 核心 CWT 容器 | 内存配置模型 | 核心职责 |
| :--- | :--- | :--- | :--- |
| `scopes.cwt` | `scopes` | `CwtScopeConfig` | 声明游戏支持的具体作用域类型、别名集合（`aliases`）与单父继承关系（`is_subscope_of`）。 |
| `scope_groups.cwt` | `scope_groups` | `CwtScopeGroupConfig` | 声明作用域分组（如 `[country, federation]`），用于规则期望作用域集合约束。 |
| `system_scopes.cwt` | `system_scopes` | `CwtSystemScopeConfig` | 声明引擎支持的系统作用域（`This`, `Root`, `From` 等）及其基类型（`base`）。 |
| `links.cwt` | `links` | `CwtLinkConfig` | 声明链式表达式的切换链接。配置参数包括 `input_scopes`、`output_scope`、前缀与数据源。 |
| `localisation_*.cwt` | `localisation_links` 等 | `CwtLocalisationCommandConfig` 等 | 驱动本地化命令文本（`[...]`）的作用域切换与提升字段。 |

### 2.2 规则选项元数据（Option Metadata）

通过在 CWT 规则上附加 `##` 选项注释，精确控制成员的作用域行为：

- **`## push_scope = <scope>`**：将进入当前属性块后的上下文显式切换为指定作用域，并把原上下文推入 `prev` 栈。
- **`## replace_scopes = { ... }`**：替换上下文中的命名槽映射（例如 `this = country`、`root = character`、`from = planet`）。**注意：仅支持 `this`、`root` 与 `from*`，不支持替换 `prev*`**。
- **`## scope = <scope>` / `## scopes = { ... }`**：为触发器或效果别名规则声明允许调用的输入作用域集合。若调用处的作用域上下文不匹配该集合，检查工具将上报作用域不匹配警告。

---

## 三、作用域上下文与传播推导机制

### 3.1 `ParadoxScopeContext` 数据结构

`ParadoxScopeContext` 是不可变的链式树形上下文容器（位于 `icu.windea.pls.model.scope.ParadoxScopeContext`）：

```text
ParadoxScopeContext
├─ scope: ParadoxScope                  // 当前作用域 (This)
├─ root: ParadoxScope?                  // 根作用域 (Root)
├─ from: ParadoxScopeContext?           // 来源上下文 (From 链)
├─ prev: ParadoxScopeContext?           // 前置上下文栈 (Prev 链)
├─ map: Map<String, String>?            // 显式命名的系统作用域映射
├─ parent: ParadoxScopeContext?         // 外层词法父上下文
└─ promotions: Set<String>?             // 本地化命令链带来的作用域提升集合
```

### 3.2 上下文下钻与传播链

1. **层级下钻 (`resolveNext`)**：当通过静态链接、动态链接或 `push_scope` 进入下一层级时，当前上下文的 `scope` 成为新上下文的 `this`，旧上下文被链接为新上下文的 `prev`，`root` 与 `from` 引用继续向深层透传。
2. **点号链式传播**：在解析复杂表达式（如 `a.b.c`）时，`ParadoxScopeService.evaluateScopeContextFromNode` 依次处理每个节点。首个节点从当前物理环境上下文获取输入，后继节点以上一节点的输出作为输入，最终节点计算出的上下文作为整段表达式的产出。

### 3.3 四类合并索引推断管线

为了在缺少显式 CWT 规则或定义未显式声明作用域时推导出正确的上下文，插件实现了基于全局用法的推断系统。该系统依赖于 4 类轻量合并索引：

1. **`ScopeInferrableDefinition`**：为封装触发器（`scripted_trigger`）与封装效果（`scripted_effect`）收集所有调用处的外部传入上下文并做并集推导。
2. **`EventInOnAction`**：从动作触发（`on_action`）的调用点反向收集其分发给目标事件的上下文环境。
3. **`EventInEvent`**：从父事件触发子事件的调用链中推导子事件的执行上下文。
4. **`OnActionInEvent`**：从事件中直接调用特定动作触发的位置提取上下文信息。

推断结果通过 `ParadoxScopeMergeService.mergeScopeContext` 进行安全收敛。若不同调用点传入冲突的作用域类型且无法通过继承树提升至共同父类型，结果将优雅降级为 `Any`，防止产生虚假报错。

---

## 四、现行约束与已知演进边界

### 4.1 现行已知局限

1. **单父继承约束**：
   - 当前 `CwtScopeConfig.is_subscope_of` 仅支持读取单个字符串。
   - 规则若声明多父作用域集合（如 CWTools 测试中的 `is_subscope_of = { parent1 parent2 }`），插件仅解析首个或忽略，不支持多重继承图。
2. **推断管线默认关闭**：
   - 考虑到大中型模组（数万个脚本文件）中跨文件用法聚合带来的索引开销，4 类推断管线在 `ChronicleSettings.state.inference` 中**默认关闭**，需用户在设置中主动开启。
3. **动态值作用域推断尚未实现**：
   - 动态变量或事件目标（`event_target:x`）的作用域当前主要依赖规则静态声明或透传；从代码写入点反向推断变量作用域类型的特性仍处于 TODO 状态。
4. **`prev` 栈的传播边界**：
   - `replace_scopes` 规则选项明确不支持覆写 `prev` 栈；在多重分支合并（`mergeScopeContext`）时，`prev` 栈通常被截断或丢弃，仅保留 `this`、`root` 与 `from`。

### 4.2 原始标量作用域（Primitive Scopes）的类型边界

在 VIC3 等较新游戏中，官方脚本引入了 `none`、`value`、`boolean`、`flag` 等无对象或标量上下文：
- **现行处理模式**：插件将它们与普通实体作用域同等视作字符串 ID（`ParadoxScope.Default`）。
- **外部工具差异**：外部工具（如 Tiger）在类型系统底层严格区分“实体数据库作用域（Database Scope）”与“原始标量作用域（Primitive Scope）”，并在类型检查中拒绝跨范畴切换；插件目前依赖 CWT 规则匹配器的宽松策略进行兼容。

---

## 五、外部工具与模型对照

| 概念维度 | Paradox Chronicle | CWTools | Tiger (ck3/vic3-tiger) |
| :--- | :--- | :--- | :--- |
| **作用域类型建模** | `ParadoxScope` 单值 + 继承图提升模拟 | `ScopeResult` / 基础作用域模型 | 静态枚举 + 区分 Primitive 与 Database 作用域 |
| **并集作用域** | 无显式 Union 类型，通过 `is_subscope_of` 匹配/合并模拟 | 支持简单的规则级多父声明 | 显式构建作用域层级树与多重兼容检查 |
| **链式调用** | 复杂表达式（`ParadoxScopeFieldExpression`）逐节点推导 | 规则解析器内置处理 | `context.rs` 栈式状态机跟踪 |
| **用法推断** | 4 类合并索引按需推导（默认关闭） | 静态离线全量遍历 | 离线静态双向类型推导 |
