package icu.windea.pls.model.scope

import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.toCapitalizedWords

object ParadoxScopeId {
    const val unknownScopeId = "?"
    const val anyScopeId = "any"
    const val allScopeId = "all"
    val anyScopeIdSet = setOf(anyScopeId)

    /**
     * 得到规范化后的作用域的 ID（snake_case）。
     */
    @Optimized
    fun getId(scope: String): String {
        val scopeId = scope.lowercase().replace(' ', '_').optimized() // optimized to optimize memory
        // "all" scope are always resolved as "any" scope
        if (scopeId == allScopeId) return anyScopeId
        return scopeId
    }

    /**
     * 得到用于展示的作用域的名字（Capitalized Words）。
     */
    @Optimized
    fun getName(scope: String, configGroup: CwtConfigGroup): String {
        // handle "any" and "all" scope
        if (scope.equals(anyScopeId, true)) return "Any"
        if (scope.equals(allScopeId, true)) return "All"
        // a scope may not have aliases, or not defined in scopes.cwt
        return configGroup.scopes[scope]?.name
            ?: configGroup.scopeAliasMap[scope]?.name
            ?: scope.toCapitalizedWords().optimized() // optimized to optimize memory
    }

    fun isUnsure(scopeId: String): Boolean {
        return scopeId == unknownScopeId || scopeId == anyScopeId || scopeId == allScopeId
    }
}
