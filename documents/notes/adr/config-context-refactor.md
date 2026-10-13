# 重构规则上下文，以提供更严格的对无法解析的表达式的代码检查

> 所属模块：`后端 > 匹配与解析`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v3.0.2
> - 日期：2026-08-20
> - 状态：已实施
> - 相关问题：https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/386

说明：
- 由于目前未区分“当前不存在上下文规则，但期望存在”这种特殊情况，此时代码检查 `UnresolvedExpressionInspection` 并不会提供必要的报错信息。
- 例如，假设 `root = {...}` 中仅能使用符合条件的属性（如 `root = { k = v }`），而实际使用了直接的值（如 `root = { v }`），此时并不会报错。
- 这可能会导致游戏解析器抛出级联错误，因为它通常只是丢弃一个无效的词元（“token”应翻译为“词元”）并再次尝试，这可能使其在保留上下文的同时跳出 `{}` 块，从而误解文件的其余部分。
- 插件应将此检测为错误。然而，由于架构上的限制，目前无法做到。
- 需要考虑重构规则上下文的相关代码，以及相关代码检查的实现代码。

我不理解……按照常理来说，它应该直接跳过，同时保留在当前深度，不应该也会跳出 `{}` 块啊？
然而，游戏引擎的解析器的行为逻辑是一回事，插件的代码检查的行为逻辑是另一回事。插件应尽其所能。

思路1：让 `configContext.getConfigs` （从规则上下文得到作为上下文的一组规则）改为返回 `List<CwtMemberConfig<*>>?`
- 如果返回 `null` 则表示不存在，并且不一定期望存在。如果返回空列表则表示不存在，并且期望存在（已经预先执行了一定的匹配逻辑，导致打平后被完全过滤，应被报告为无法解析的表达式）。
- 需要兼容成员树（不仅是当前成员）中带参数的情况。
- 需要兼容成员树中匹配到了 `$any` 数据类型的情况。
- 进行代码检查时，遍历一个节点时可能不保证会先遍历其父节点，因此可能无法通过本地堆栈的方式判断某个表达式是否真的无法被解析（上层成员可能带参数）。需要验证。

思路2：在 `configContextProvider` 中新增方法，以决定是否跳过 `UnresolvedExpressionInspection`
- 如果 `configContext.inRoot()` 为 `false`，则需要跳过。这意味着当前位于某种根上下文之外，例如位于定义声明之外。我们目前不检查这种情况，但未来需要考虑进一步完善（并且，需要使用不同的报错信息）。

计划：
- [x] 对于 `CwtConfigContext` 和 `CwtConfigContextProvider`，新增方法 `skipUnresolvedExpressionCheck`，以在必要时跳过无法解析的表达式的代码检查。
  - 例如，当前位于根上下文之外时。
- [x] ~~对于 `CwtConfigContext` 和 `CwtConfigContextProvider`，方法 `getConfigs` 可能需要改为返回 `List<CwtMemberConfig<*>>?`。~~ - 不再需要
  - 如果实施了后续的2个计划，由于应该已经覆盖了所有需要处理的边界情况，这一项可能是不需要的。
- [x] 如果在解析得到上下文规则或匹配的规则时，父节点上存在 `$any` 数据类型，展开后得到的规则可能需要始终保留 `$any` 数据类型（`$any = $any` 或 `$any`）。
  - 这里保留的规则是模拟的规则（mockConfig），规则文件中不存在声明处。
  - 这是一种需要处理的边界情况，否则，对于代码检查或者某些信息提示，可能导致一些细节上的误报。
- [x] 如果在解析得到上下文规则或匹配的规则时，成员路径上带参数，也要尽可能地进行展开和匹配。
  - 解析得到上下文规则时，如果成员路径上带参数，或者说当前表达式带参数，不需要也不应当进行特殊处理，除非需要对键进行进一步的匹配（`matchConfigsForConfigContext`）。
  - 对于带参数的属性键的匹配，由于这实际上是更进一步的匹配，考虑从 `collectConfigsForConfigContext`，改为放到 scriptMatchOptimizer 中。
  - 对于带参数的属性键的匹配，如果其参数的类型可以推断，适用后仍然无法精确匹配（这意味着最终得到的匹配的规则是唯一的），不再进行特殊处理。
- [x] 如果表达式是一个特殊的标签（如 typeKeyPrefix），则需要跳过检查。

附加：
- [ ] 即使当前位于根上下文之外，如果必要，也通过 `UnresolvedExpressionInspection` 提供必要的报错信息。
  - 由于这项检查是非常取决于规则文件的完备性的，即使未来实现了，可能也不应当默认启用。例如，当前文件中可能存在其他未被规则文件覆盖到的定义声明。

相关代码位置：
- [x] `icu.windea.pls.lang.resolve.CwtConfigContext`
- [x] `icu.windea.pls.ep.resolve.config.CwtConfigContextProvider`
- [x] `icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection`
- [x] `icu.windea.pls.lang.resolve.ParadoxConfigService.flattenConfigsForConfigContext`
- [x] `icu.windea.pls.lang.resolve.ParadoxConfigService.resolveConfigs`
- [x] `icu.windea.pls.lang.inspections.ParadoxExpressionInspectionService.checkForUnresolvedExpression`
