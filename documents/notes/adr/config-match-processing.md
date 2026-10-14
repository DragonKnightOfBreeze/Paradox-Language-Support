# 完善规则匹配的预处理和后续处理逻辑

> - 模块：后端 > 匹配与解析
> - 版本：v3.0.3
> - 日期：2026-09-23
> - 状态：已实施（基本完成，告一段落）

要点：
- 必须限制收集得到的匹配候选项的上限
  - 否则，例如，由于构建索引时无法访问索引，这可能会会造成最终收集了成百上千的匹配候选项……
  - 否则，例如，由于现在同样会处理带参数的表达式，如果是表达式整个作为参数的，这可能造成最终收集了更多数量的匹配候选项……

任务：
- [x] P2 如果匹配候选项存在很多个，需要考虑在超出阈值时进行回退
  - 改为直接回退为对应的 `$any = $any` 或 `$any`。
  - 如果不这样做，在极端情况下，可能会带来明显的性能问题。
  - 阈值：`icu.windea.pls.ChronicleCapabilities.General.maxMatchCandidateSize` - 来自系统属性 `chronicle.maxMatchCandidateSize`。
  - 阈值：`icu.windea.pls.ChronicleCapabilities.General.maxProcessedMatchCandidateSize` - 来自系统属性 `chronicle.maxProcessedMatchCandidateSize`。
- [x] P2 （忽略）~~如果匹配候选项存在较多个，并且表达式整个作为参数，需要考虑直接进行回退~~
  - 可以考虑直接回退为对应的 `$any = $any` 或 `$any`。
  - 也可以考虑直接回退为对应的 `$parameterized = $parameterized` 或 `$parameterized`，作为区分。
  - 如果不这样做，虽然确实可以尝试递归打平并匹配，但是可能会带来明显的性能问题。
  - 忽略：不需要进行特殊处理，如果超出阈值，照常回退即可。
- [x] P2 如果匹配得到的规则最终仍然存在多个，在一些情况下，需要进一步的过滤
  - [x] 如果需要全部类型信息，则应保留所有规则。
  - [x] 对于代码高亮，认为需要使用数据类型优先级最高的第一个规则。为了与引用解析的行为对齐，也可以考虑仅使用唯一规则。
  - [x] 对于引用解析，认为需要使用数据类型优先级最高的唯一规则。
  - [x] 对于类型信息，至少需要保留数据类型优先级最高的所有规则（目前保留所有规则）。
  - [x] 对于作用域上下文的评估（`ParadoxScopeService.evaluateScopeContextFromMember`），认为需要使用数据类型优先级最高的唯一规则。
  - [ ] 对于装订线图标、内嵌提示等延伸语言功能，需要确认代码和行为，并进行分析和决策。
  - [ ] 检查 `ParadoxConfigManager.getConfigs` 和 `ParadoxConfigService.getConfigs` 的所有用法

相关代码位置：
- [x] `icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectCandidate`
- [x] `icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectProcessedCandidate`
- [x] `icu.windea.pls.config.util.CwtConfigManager.selectFirstPrioritizedConfig`
- [x] `icu.windea.pls.config.util.CwtConfigManager.selectSinglePrioritizedConfig`
