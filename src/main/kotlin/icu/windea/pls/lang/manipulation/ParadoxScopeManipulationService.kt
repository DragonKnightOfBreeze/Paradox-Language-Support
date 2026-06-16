package icu.windea.pls.lang.manipulation

import icu.windea.pls.core.collections.orNull
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.scope.ParadoxScopeConstants
import icu.windea.pls.model.scope.ParadoxScopeContext

object ParadoxScopeManipulationService {
    fun mergeScopeId(scopeId: String?, otherScopeId: String?): String? {
        if (scopeId == otherScopeId) return scopeId
        if (scopeId == ParadoxScopeConstants.anyScope || otherScopeId == ParadoxScopeConstants.anyScope) return ParadoxScopeConstants.anyScope
        if (scopeId == ParadoxScopeConstants.unknownScope || otherScopeId == ParadoxScopeConstants.unknownScope) return ParadoxScopeConstants.unknownScope
        if (scopeId == null) return otherScopeId
        if (otherScopeId == null) return scopeId
        return null
    }

    @Suppress("unused")
    fun mergeScope(scope: ParadoxScope?, otherScope: ParadoxScope?): ParadoxScope? {
        if (scope == otherScope) return scope ?: ParadoxScope.Unknown
        if (scope == ParadoxScope.Any || otherScope == ParadoxScope.Any) return ParadoxScope.Any
        if (scope == ParadoxScope.Unknown || otherScope == ParadoxScope.Unknown) return ParadoxScope.Unknown
        if (scope == null) return otherScope
        if (otherScope == null) return scope
        return null
    }

    fun mergeScopeContext(scopeContext: ParadoxScopeContext?, otherScopeContext: ParadoxScopeContext?, orUnknown: Boolean = false): ParadoxScopeContext? {
        val m1 = scopeContext?.toScopeIdMap(showPrev = false).orEmpty()
        val m2 = otherScopeContext?.toScopeIdMap(showPrev = false).orEmpty()
        val merged = mergeScopeContextMap(m1, m2, orUnknown) ?: return null
        return ParadoxScopeContext.resolve(merged)
    }

    fun mergeScopeContextMap(map: Map<String, String>, otherMap: Map<String, String>, orUnknown: Boolean = false): Map<String, String>? {
        val c = ParadoxScopeConstants
        val result = mutableMapOf<String, String>()
        fun putScopeId(key: String) = mergeScopeId(map[key], otherMap[key])?.let { result[key] = it }
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
