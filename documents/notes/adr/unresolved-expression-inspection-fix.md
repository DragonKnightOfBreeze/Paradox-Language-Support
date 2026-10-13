# 修复来自 `UnresolvedExpressionInspection` 的误报

> 所属模块：`后端 > 语言功能支持`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v3.0.2+
> - 日期：2026-09-08+
> - 状态：告一段落（待补充测试）

计划：
- [x] 如果注入的上下文中不存在上下文规则（例如参数值中不存在可以推断得到的规则），应保证返回空列表，否则会引发误报
  - 引入 `CwtConfigManipulationService.skipMergedConfigs(mergedConfigs)`，以在完成规则的合并后，在返回合并结果前，决定是否需要直接跳过合并结果（即返回空列表）。
- [x] `ParadoxDefaultExpressionParameterInferredConfigProvider` 应忽略条件参数（conditionParameter）以及表达式整个作为参数的普通参数（parameter），否则可能会引发误报
  - 在这些情况下，可能对应一个表达式，一组语句，甚至是任意一段脚本片段。
- [x] 对于脚本文件，`ParadoxExpressionInspectionService.checkForUnresolvedExpression(element, context)` 应在当前表达式整个作为参数时，直接跳过检查
  - 原因同上。
- [x] 需要重构 `ParadoxMatchOptions`，从而可以在必要时采用更合适的匹配选项/匹配策略。
  - 将属性 `lenient` 重命名为 `skipBlock`（对于 `ParadoxMatchResult.LazyBlockAwareMatch`，直接认为匹配）。
  - 将属性 `fallback` 重命名为 `lenient`（匹配时，如果无法进一步匹配，则需要采用宽松匹配策略，返回上一步已匹配得到的规则）。
  - 新增属性 `forExpression`（仅匹配表达式本身，这意味着匹配属性时，不要求严格匹配属性值）。
  - 某些场景，例如解析表达式引用、检查无法解析的表达式，需要改为使用 `lenient = false, forExpression = true`，而非 `lenient = isKey`。
- [x] 需要检查 `lenient = false` 的场景（`resultForKey.isEmpty()` 时，不再会回退）
- [x] 需要检查 `lenient = isKey` 的场景（通常可以替换为 `lenient = false, forExpression = true`）

任务（2026-09-08+）：
- [ ] 补充测试 `icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspectionTest`

```
# status: resolved
# stellaris: common/scripted_effects/psionic_auras_scripted_effects.txt
setup_aura_clashing_monthly = {
	set_timed_star_flag = {
		flag = aura_clashing_on_monthly_check
		days = 20 #to fit within on_monthly (can be changed for AIs if needed)
	}
	setup_aura_clashing = {
		PRE_LOOP_CONDITIONS = "<error>NOT</error> = { has_star_flag = aura_clashing_on_monthly_check }" # should be no errors
		PRE_LOOP_EFFECTS = "<error>clear_aura_clashing_flags</error> = yes" # should be no errors
	}
}
```

```
# status: resolved
# stellaris: common/scripted_effects/00_scripted_effects.txt
give_next_breakthrough_effect = {
	inverted_switch = {
		trigger = has_technology
		tech_$AREA$_1 = {
			give_breakthrough_tech_option_or_progress_effect = <error>{ TECH = tech_$AREA$_1 }</error> # undesired
			# <error>give_breakthrough_tech_option_or_progress_effect</error> = { TECH = tech_$AREA$_1 } # should be (`give_breakthrough...` is likely a unresolved predefined effect)
		}
		# 。。。
	}
}
```
