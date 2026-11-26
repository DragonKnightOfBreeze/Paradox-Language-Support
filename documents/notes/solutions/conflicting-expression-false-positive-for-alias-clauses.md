# 作用域链接被检查出“匹配多个存在冲突的规则”（子句规则的匹配逻辑未考虑子句内容）

> - 模块：后端（匹配/代码检查）
> - 版本：v3.0.6
> - 日期：2026-11-07
> - 标签：`ParadoxMatchFactory` / `ConflictingExpressionInspection` / 别名规则 / `CwtSeparatorType` / 回归测试

## 背景

- 3.0.6-dev 的 CI（`Build` 工作流中的 `Test` 任务，即 `./gradlew check -Dchronicle.test.include.remote=true`）从 2026-11-07 起持续失败，最后两次成功/失败的运行分别是：
  - 成功：`41700819602`（master，2026-11-03）；
  - 失败：`42370916949`（master）、`42375658568`（dev/3.0.6-dev）。
- 定位方式：先在本地按 CI 的方式复现（使用同一份 CWT 规则，即 `cwtools-*-config` 的 `master` 分支 zip，与同一份代码），再与 CI 的测试报告交叉确认；两者报告的偏移量完全一致（254 / 368 / 517）。
- 复现结果：`icu.windea.pls.test.issues.Issue284Test` 失败，`testScopeLinkInTrigger` 报告多余的警告：
  ```
  extra (6:5/5): 'owner' (`owner = {...}` matches multiple configs with conflicts) [WARNING_ATTRIBUTES]
  extra (13:5/10): 'root.owner' (...)
  extra (20:5/20): 'event_target:x.owner' (...)
  ```

## 分析

### 原因

- 触发条件是上游规则仓库的内容变更：`cwtools-vic3-config` 的提交 `fa87c083`（`Updated to 1.15`，2026-11-05）在 `config/scope_links.cwt` 中新增了一行：
  ```cwt
  alias[trigger:scope_field] == { alias_name[arithmetic_operation] = alias_match_left[arithmetic_operation] }
  ```
  这使得同一个属性键（如作用域链接 `owner`）在触发器上下文中同时拥有两个“值为块”的候选子句：
  - `alias[trigger:scope_field] = { alias_name[trigger] = alias_match_left[trigger] }`
  - `alias[trigger:scope_field] == { alias_name[arithmetic_operation] = alias_match_left[arithmetic_operation] }`
- 插件侧的根本问题在 `ParadoxMatchFactory.matchesBlock`：
  ```kotlin
  val keys = CwtConfigManager.getWithinBlockKeys(config)
  if (keys.isEmpty()) return true // 子句中没有常量键时直接认为匹配
  ```
  该启发式原本只会收集“常量键”（`CwtDataTypes.Constant` 且基数必须的键），而别名引用（`alias_name[...]`）不是常量键，因此这两个子句的 `keys` 均为空，于是任意块内容都会被认为同时匹配这两个子句。
- 于是 `ConflictingExpressionInspection` 看到同一表达式匹配到多个（子句约束不同的）规则，报出“匹配多个存在冲突的规则”。这属于误报：`owner = { always = yes }` 只可能匹配触发器子句。

### 为什么之前没有暴露

- 在规则变更之前，`trigger:scope_field` 只有唯一一个“值为块”的子句，即使匹配逻辑很宽松也只会得到一个候选，因此不会触发冲突检查。
- 也说明该问题不是回归提交引入的，而是“规则内容变化 + 匹配启发式过弱”共同导致。

## 解决方案

- 在 `ParadoxMatchFactory.matchesBlock` 中补充“子句没有常量键”时的兜底判断：改为用块中的成员逐个匹配子句规则（键表达式），仅当至少一个成员能匹配时才认为该子句匹配。
  ```kotlin
  val childConfigs = config.configs.orEmpty()
  if (childConfigs.isEmpty()) return true
  val members = element.members(conditional = true, inline = true)
  if (members.none()) return true // 空块无法按内容区分，视为匹配（此时确实存在歧义）
  return members.any { member ->
      member is ParadoxScriptProperty && childConfigs.any { child -> matchesBlockChild(member, child, configGroup) }
  }
  ```
- `matchesBlockChild` 只接受“可用作判断依据”的匹配结果：精确匹配（含宽松精确匹配）与需要进一步检测的匹配；显式排除通配符匹配、部分匹配和回退匹配（`WildcardMatch` `LenientWildcardMatch` `PartialMatch` `FallbackMatch`）。否则别名名称为作用域引用时，任意文本都会被通配符匹配（`always` 匹配 `alias_name[arithmetic_operation]` 就是 `LenientWildcardMatch`）。
- 结果：`owner = { always = yes }` 只匹配触发器子句，冲突消失；`var:test = { root = root }`（作用域 `root` 同时可作为算术运算的操作数）与 `var:test = {}`（空块）仍然判定为存在歧义并保留警告。

## 注意事项

- 该修复使“块内容可以明确区分子句”的场景不再报冲突，因此 `Issue142Test` 中 `var:test = { always = yes }` 不再产生警告（已同步更新测试数据与 KDoc）；`var:test = { root = root }` 与 `var:test = {}` 的警告保持不变。
- 判断依据是“键”而非“值”：`matchesBlock` 只用于区分同一属性键下的多个子句规则，因此不需要（也不应该）在此处做完整的值与作用域匹配，避免引入递归与性能问题。
- 若后续仍有类似误报，优先检查：子句是否全部由别名引用/模板表达式构成、匹配结果是否只是通配符或回退匹配。

## 验证

- `./gradlew test --tests "icu.windea.pls.test.*"`：通过（含 `Issue284Test`、`Issue142Test` 等回归测试）。
- `./gradlew test --tests "icu.windea.pls.lang.inspections.script.expression.ConflictingExpressionInspectionTest"`：通过。
- CI：`dev/3.0.6-dev` 修复后的运行 `42389551512`，`Build` / `Test` / `Verify plugin` / `Benchmark` 四个 job 全部通过。
- 补充的用例：`src/test/testData/issues/284/common/scripted_triggers/test_trigger.test.txt` 新增“作用域链接的块中再嵌套触发器块”的场景。

## 待评估 / 扩展内容

> 以下几项与本次修复相邻，但本轮**刻意不改动代码**，只记录事实与待评估的方向。

### 1. block 匹配逻辑分散在两处（此前已登记）

- `ideas.md`（`2026-09-21`）已有两项 P3：
  - 考虑进一步完善 `ParadoxScriptExpressionBlockMatchOptimizer`；
  - 考虑进一步完善 `ParadoxMatchResultProvider.forBlock`，或者直接将相关逻辑整合到前者。
  - 命名备注：后者的实际类型是 `ParadoxMatchResultFactory.forBlock`（`icu.windea.pls.lang.match.util`），`ParadoxMatchResultProvider` 已不存在，属于笔记中的命名漂移。
- `roadmap.md`（`v3.0.2`，[#386](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/386)）指出“这实际上是更进一步的匹配，考虑从 `getConfigsForConfigContext` 改为放到 scriptMatchOptimizer 中”；`v3.0.3` 一节记录了同族的 `ParadoxScriptExpressionParameterizedMatchOptimizer`。
- 现状：两条路径确实都在回答“块内容能否区分子句”，且**共用** `ParadoxMatchFactory.matchesBlock`：
  - `ParadoxMatchResultFactory.forBlock` → `ParadoxMatchResult.LazyBlockAwareMatch { matchesBlock(...) }`，在候选项处理阶段（`ParadoxMatchCandidateService.processMain` → `processLenient`）参与；
  - `ParadoxScriptExpressionBlockMatchOptimizer`（`icu.windea.pls.ep.match.expression`）在匹配之后的 `optimize` 阶段（`ParadoxConfigService.matchConfigs` → `ParadoxConfigMatchService.optimize`）按 `config.key` 分组收敛同键的块值规则，内部同样经由 `matchScriptExpression` 落到 `forBlock`。
  - 因此把启发式修在 `matchesBlock` 这一个点上，两条路径会同时受益；但两者的“分组 / 收敛 / 回退”骨架仍然重复——这正是上面那条“整合”待办的动机。
- 两处可继续确认的细节：
  - `ParadoxMatchCandidateService.processLenient` 中，当多个 `LazyBlockAwareMatch` 候选项**全部**匹配失败时会退化为“只取第一个”（代码内已有 `NOTE 3.0.3 may collect first or all` 的注释，并点名了 `forBlock` / `BlockMatchOptimizer` 的重构时机）。本次修复让“部分匹配、部分不匹配”成为常态，该回退分支的触发概率下降，但在语义模糊的场景（如空块）仍然存在。
  - `ParadoxScriptExpressionBlockMatchOptimizer` 在 `filtered` 非空时直接以 `filtered` 作为返回结果，这意味着输入中**非块值**的候选项会被一并丢弃；是否为预期行为需要确认。

### 2. 规则文件中的 `==` 尚未参与匹配

- `CwtSeparatorType`（`icu.windea.pls.model.type`）有三个取值：`Equal("=")`、`NotEqual("!=")`、`DoubleEqual("==")`（注释为 `matches comparison operators`）。
- 参考文档 `docs/zh/ref-syntax.md`（CWT 语言 → 基本元素）说明：`==` 类似 `=`，但表示匹配的脚本属性应使用比较运算符（如 `=` `!=` `>=`），而不是赋值运算符；并明确写到“如果规则文件中使用了 `==`，插件默认不会严格检查匹配的脚本属性的分隔符是否正确。这项检查可在设置页面中显式启用，然而，取决于规则的完善程度，这可能引发额外的误报。”
- 也就是说，`==` 目前**只影响代码检查，不影响匹配**：
  - `DoubleEqual` 的消费方只有 `ParadoxSyntaxService.isComparisonOperatorValid(config)`（判定条件为 `config is CwtPropertyConfig && separatorType == DoubleEqual`），由 `ParadoxComparisonOperatorChecker` 在语义层调用（文法层另有一套“键与值可表示数值”的检查）；
  - 开关为 `ChronicleConfigSettings.state.features.checkComparisonOperators`，默认 **`false`**；
  - 匹配侧（`ParadoxConfigService`、`ParadoxMatchFactory.matchesBlock`、各 `ParadoxScriptExpressionMatchOptimizer`）完全不读取 `separatorType`。
- 一个与本次问题直接相关的发现：**`==` 在别名内联后会丢失**。
  - `CwtAliasConfig` 不暴露 `separatorType`（`CwtAliasConfigImpl` 只持有 `name` / `subName` 和作用域相关信息）；
  - `CwtConfigInlineService.inlineAlias` 以 `CwtPropertyConfig.copy(sourceConfig = config, …)` 复制**用法处**的属性（`alias_name[trigger] = …`，分隔符为 `=`），因此 `scope_links.cwt` 中 `alias[trigger:scope_field] == { … }` 的 `==` 不会进入内联后的规则；
  - 结果是 `isComparisonOperatorValid` 在内联规则上无法看到声明处的 `==`。
- 另一处不对称：分隔符参与了“规则身份”的构造——`CwtConfigKeyManager.getDistinctKey`（规则去重 / 合并）与 `getIdentifierKey`（子句出现次数的缓存键）都会写入 `separatorType.text`。即 `=` 与 `==` 被视为**不同的规则**，但这个差异在匹配阶段又被抹平了。
- 补充说明：`Equal` / `NotEqual` 另有用途（例如 `CwtOptionMetadataResolutionScope.resolvePredicate` / `resolveTypeKeyFilter` 用 `separatorType == Equal` 决定 `ReversibleValue` 的正负，对应选项行上的 `=` / `!=`），与本节的 `==` 无关，列在这里以免把“分隔符只用于 `==` 检查”当成一个错误印象。
- 待评估（来自尚未同步到 `ideas.md` 的想法）：
  - 让 `CwtSeparatorType.DoubleEqual`（规则文件中显式使用 `==`，而非常规的 `=`）可以影响规则匹配；
  - 该能力已经可以影响代码检查，但未默认启用，需要评估是否默认启用；
  - 若要在匹配层利用它，可考虑的方向包括“在同一属性键的多个块子句中优先选择分隔符与脚本属性实际分隔符一致的子句”，以及在别名内联时保留声明处的分隔符——但都需要先评估对现有规则集的影响面。

## 参考链接

- 规则变更：https://github.com/DragonKnightOfBreeze/cwtools-vic3-config/commit/fa87c083f3
- 相关问题：https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/284
- 相关问题：https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/142
