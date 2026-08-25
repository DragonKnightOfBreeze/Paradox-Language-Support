package icu.windea.pls.lang.scope

import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.equalsFast
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.scope.ParadoxScopeConstants
import icu.windea.pls.model.scope.ParadoxScopeContext

@Suppress("unused")
@Optimized
object ParadoxScopeMergeService {
    // NOTE 3.0.2 support to merge union scopes (promote to super scopes if necessary)
    // TODO 3.0.2 introduce `ParadoxScopeMergeContext` (required for merging union scopes, so fast return may not applicable)

    // 作用域的合并逻辑（scope -> scopeToMerge）：
    // - scope 是输入的数据（从上下文中获取或推断）。
    // - scopeToMerge 是另一个输入的数据（从上下文中获取或推断）。
    // - 如果两者都为 `null`，则返回 `null`。
    // - 如果其中一项为 `null`，则返回另一项。
    // - 如果其中一项为 `any`，则返回 `any`。
    // - 如果其中一项为 `?`，则返回 `?`。
    // - 如果两者的 ID 完全匹配，则返回第一个（比较字符串，值相等，不忽略大小写）。
    // - 如果存在别名（来自 `scopeConfig.aliases`），则尝试用这些别名进行完全匹配，返回第一个。
    // - 如果存在父作用域（来自 `scopeConfig.is_subscope_of`），则尝试使用这些父作用域的 ID 和别名进行完全匹配。这需要递归进行。

    /**
     * 合并作用域。兼容通配形式和别名形式。兼容继承关系（子作用域会提升到父作用域）。
     *
     * [scope] 是输入的作用域，[scopeToMerge] 是另一个输入的作用域。
     * 两者都应是规范化后的作用域的 ID。
     *
     * @see ParadoxScope
     * @see ParadoxScopeContext
     */
    fun mergeScope(scope: String?, scopeToMerge: String?, configGroup: CwtConfigGroup): String? {
        if (scope == null && scopeToMerge == null) return null
        if (scope == null || scopeToMerge == null) return scope ?: scopeToMerge
        if (scope.equalsFast(scopeToMerge)) return scope
        val anyScope = ParadoxScopeConstants.anyScope
        if (scope.equalsFast(anyScope) || scopeToMerge.equalsFast(anyScope)) return anyScope
        val unknownScope = ParadoxScopeConstants.unknownScope
        if (scope.equalsFast(unknownScope) || scopeToMerge.equalsFast(unknownScope)) return unknownScope
        mergeScopeFromModel(ParadoxScope.resolve(scope), ParadoxScope.resolve(scopeToMerge), configGroup)?.let { return it.id }
        return null
    }

    fun mergeScope(scope: ParadoxScope?, otherScope: ParadoxScope?): ParadoxScope? {
        if (scope == otherScope) return scope ?: ParadoxScope.Unknown
        if (scope == ParadoxScope.Any || otherScope == ParadoxScope.Any) return ParadoxScope.Any
        if (scope == ParadoxScope.Unknown || otherScope == ParadoxScope.Unknown) return ParadoxScope.Unknown
        if (scope == null) return otherScope
        if (otherScope == null) return scope
        return null
    }

    private fun mergeScopeFromModel(scope: ParadoxScope, scopeToMerge: ParadoxScope, configGroup: CwtConfigGroup): ParadoxScope? {
        // optimize: access scope model and check scope indexes for better performance
        val scopeModel = configGroup.scopeModel
        // bidirectional
        run {
            val matched = scopeModel.base2MatchedScopes.get(scope.index)
            if (matched.isNullOrEmpty()) return@run
            if (matched.contains(scopeToMerge.index)) return scopeToMerge
        }
        run {
            val matched = scopeModel.base2MatchedScopes.get(scopeToMerge.index)
            if (matched.isNullOrEmpty()) return@run
            if (matched.contains(scope.index)) return scope
        }
        return null
    }

    fun mergeScopeContext(scopeContext: ParadoxScopeContext?, scopeContextToMerge: ParadoxScopeContext?, configGroup: CwtConfigGroup, orUnknown: Boolean = false): ParadoxScopeContext? {
        val scopeIdMap = scopeContext?.toScopeIdMap(showPrev = false).orEmpty()
        val scopeIdMapToMerge = scopeContextToMerge?.toScopeIdMap(showPrev = false).orEmpty()
        val merged = mergeScopeContextMap(scopeIdMap, scopeIdMapToMerge, configGroup, orUnknown) ?: return null
        return ParadoxScopeContext.resolve(merged)
    }

    fun mergeScopeContextMap(map: Map<String, String>, otherMap: Map<String, String>, configGroup: CwtConfigGroup, orUnknown: Boolean = false): Map<String, String>? {
        val c = ParadoxScopeConstants
        val result = mutableMapOf<String, String>()
        fun putScopeId(key: String) = mergeScope(map[key], otherMap[key], configGroup)?.let { result[key] = it }
        putScopeId(c.thisScope)
        putScopeId(c.rootScope)
        putScopeId(c.fromScope)
        putScopeId(c.from2Scope)
        putScopeId(c.from3Scope)
        putScopeId(c.from4Scope)
        putScopeId(c.prevScope)
        putScopeId(c.prev2Scope)
        putScopeId(c.prev3Scope)
        putScopeId(c.prev4Scope)
        if (orUnknown) {
            val thisScope = result[c.thisScope]
            if (thisScope == null || thisScope == c.unknownScope) result[c.thisScope] = c.unknownScope
            val rootScope = result[c.rootScope]
            if (rootScope == null || rootScope == c.unknownScope) result[c.rootScope] = c.unknownScope
        }
        return result.orNull()
    }
}
