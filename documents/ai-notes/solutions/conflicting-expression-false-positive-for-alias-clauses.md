# 作用域链接被检查出“匹配多个存在冲突的规则”（子句规则的匹配逻辑未考虑子句内容）

> - 模块：后端（匹配/代码检查）
> - 版本：v3.0.6
> - 日期：2026-11-07
> - 标签：`ParadoxMatchFactory` / `ConflictingExpressionInspection` / 别名规则 / 回归测试

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

## 参考链接

- 规则变更：https://github.com/DragonKnightOfBreeze/cwtools-vic3-config/commit/fa87c083f3
- 相关问题：https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/284
- 相关问题：https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/142
