# 修复来自 `MissingExpressionInspection` 的误报

> - 模块：后端 > 语言功能支持
> - 版本：v3.0.3+
> - 日期：2026-09-21+
> - 状态：告一段落（待补充测试）

计划：
- [x] 对于注入的上下文，总是应当在顶层跳过此项检查

任务（2026-09-21+）：
- [ ] 补充测试 `icu.windea.pls.lang.inspections.script.expression.MissingExpressionInspectionTest`

```
# status: resolved
# stellaris, common/traits/18_nomads_leader_traits.txt

leader_trait_paladin_ace = {
	inline_script = {
		script = trait/icon_specialist
		CLASS = commander
		ICON = <error>"GFX_leader_trait_paladin_ace"</error> # should be no errors (skipped by the config context)
		RARITY = veteran
		COUNCIL = no
		TIER = none
	}
	# ...
}
```
