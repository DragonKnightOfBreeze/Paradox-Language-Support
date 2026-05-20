package icu.windea.pls.lang.manipulation

import icu.windea.pls.core.collections.orNull
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.scope.ParadoxScopeConstants

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
            if (thisScope == null || thisScope == ParadoxScopeConstants.unknownScope) {
                result["this"] = ParadoxScopeConstants.unknownScope
            }
            val rootScope = result["root"]
            if (rootScope == null || rootScope == ParadoxScopeConstants.unknownScope) {
                result["root"] = ParadoxScopeConstants.unknownScope
            }
        }
        return result.orNull()
    }
}
