package icu.windea.pls.lang.util.manipulators

import icu.windea.pls.core.collections.orNull
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.scope.ParadoxScopeId
import icu.windea.pls.model.scope.toScopeIdMap

object ParadoxScopeManipulator {
    fun mergeScopeId(scopeId: String?, otherScopeId: String?): String? {
        if (scopeId == otherScopeId) return scopeId
        if (scopeId == ParadoxScopeId.anyScopeId || otherScopeId == ParadoxScopeId.anyScopeId) return ParadoxScopeId.anyScopeId
        if (scopeId == ParadoxScopeId.unknownScopeId || otherScopeId == ParadoxScopeId.unknownScopeId) return ParadoxScopeId.unknownScopeId
        if (scopeId == null) return otherScopeId
        if (otherScopeId == null) return scopeId
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

    fun mergeScopeContext(scopeContext: ParadoxScopeContext?, otherScopeContext: ParadoxScopeContext?, orUnknown: Boolean = false): ParadoxScopeContext? {
        val m1 = scopeContext?.toScopeIdMap(showPrev = false).orEmpty()
        val m2 = otherScopeContext?.toScopeIdMap(showPrev = false).orEmpty()
        val merged = mergeScopeContextMap(m1, m2, orUnknown) ?: return null
        return ParadoxScopeContext.get(merged)
    }

    fun mergeScopeContextMap(map: Map<String, String>, otherMap: Map<String, String>, orUnknown: Boolean = false): Map<String, String>? {
        val result = mutableMapOf<String, String>()
        mergeScopeId(map["this"], otherMap["this"])?.let { result["this"] = it }
        mergeScopeId(map["root"], otherMap["root"])?.let { result["root"] = it }
        mergeScopeId(map["prev"], otherMap["prev"])?.let { result["prev"] = it }
        mergeScopeId(map["prevprev"], otherMap["prevprev"])?.let { result["prevprev"] = it }
        mergeScopeId(map["prevprevprev"], otherMap["prevprevprev"])?.let { result["prevprevprev"] = it }
        mergeScopeId(map["prevprevprevprev"], otherMap["prevprevprevprev"])?.let { result["prevprevprevprev"] = it }
        mergeScopeId(map["from"], otherMap["from"])?.let { result["from"] = it }
        mergeScopeId(map["fromfrom"], otherMap["fromfrom"])?.let { result["fromfrom"] = it }
        mergeScopeId(map["fromfromfrom"], otherMap["fromfromfrom"])?.let { result["fromfromfrom"] = it }
        mergeScopeId(map["fromfromfromfrom"], otherMap["fromfromfromfrom"])?.let { result["fromfromfromfrom"] = it }
        if (orUnknown) {
            val thisScope = result["this"]
            if (thisScope == null || thisScope == ParadoxScopeId.unknownScopeId) {
                result["this"] = ParadoxScopeId.unknownScopeId
            }
            val rootScope = result["root"]
            if (rootScope == null || rootScope == ParadoxScopeId.unknownScopeId) {
                result["root"] = ParadoxScopeId.unknownScopeId
            }
        }
        return result.orNull()
    }
}
