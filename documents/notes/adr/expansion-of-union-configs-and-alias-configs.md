# 并集规则和别名规则的规则展开

> - 模块：后端 > 规则系统
> - 版本：v3.0.1
> - 日期：2026-07-12
> - 状态：已实施（部分）

说明：
- 对于作为上下文的规则以及匹配的规则，有时需要展开并集（`union[x]`）和别名（`alias_keys_field[x]`）
- 按照实际情况，展开为所有候选项，或者展开为实际匹配的那个规则
- 这里使用的“展开”而非“内联”，“内联”意味着在解析规则（上下文规则，或者匹配的规则）时，就会修改规则的结构，返回操作后的规则（比如对于 `alias_name[x] = alias_match_left[x]` 和 `key = single_alias_right[x]`）。
- 对于上下文规则，不能进行实际的展开（可以存在多个候选项），需要考虑在必要时遍历展开后的所有规则（考虑提供 `process` 方法或者获取此序列的方法）。
- 对于匹配的规则，需要考虑在语义匹配过程中就（将键或值的部分）内联为实际匹配的那个规则，或者在语义匹配与解析之后的阶段中，进行二次匹配。

相关代码位置：
- [x] `icu.windea.pls.config.manipulation.CwtConfigExpansionService.expandConfigExpression`
- [x] `icu.windea.pls.model.constraints.ParadoxReferenceConstraint` - 适用展开
- [x] `icu.windea.pls.ep.inspections.ParadoxIncorrectExpressionChecker` - 适用展开（大部分实现）
- [x] `icu.windea.pls.ep.inspections.ParadoxUnresolvedExpressionChecker` - 适用展开
