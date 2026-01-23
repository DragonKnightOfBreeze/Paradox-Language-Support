package icu.windea.pls.model.scope

import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.ep.resolve.scope.ParadoxOverriddenScopeContextProvider

/** 需要提升的作用域的 ID 列表。 */
var ParadoxScopeContext.promotions: Set<String> by registerKey(ParadoxScopeContext.Keys) { emptySet() }

/** 当前的作用域上下文是否是精确的 - 这意味着不需要再进一步推断其中的各个作用域。 */
var ParadoxScopeContext.isExact: Boolean by registerKey(ParadoxScopeContext.Keys) { true }

/** 当前作用域上下文对应的重载提供者。 */
var ParadoxScopeContext.overriddenProvider: ParadoxOverriddenScopeContextProvider? by registerKey(ParadoxScopeContext.Keys)
