package icu.windea.pls.lang.match

import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.scope.ParadoxScopeId
import icu.windea.pls.model.scope.promotions

object ParadoxScopeMatchService {
    fun matchesScope(scopeContext: ParadoxScopeContext?, scopeToMatch: String, configGroup: CwtConfigGroup): Boolean {
        val thisScope = scopeContext?.scope?.id
        if (thisScope == null) return true
        if (scopeToMatch == ParadoxScopeId.anyScopeId) return true
        if (thisScope == ParadoxScopeId.anyScopeId) return true
        if (thisScope == ParadoxScopeId.unknownScopeId) return true
        if (thisScope == scopeToMatch) return true
        val scopeConfig = configGroup.scopeAliasMap[thisScope]
        if (scopeConfig != null && scopeConfig.aliases.any { it == scopeToMatch }) return true

        // from promotions
        val promotions = scopeContext.promotions
        for (promotion in promotions) {
            if (promotion == scopeToMatch) return true
            val promotionConfig = configGroup.scopeAliasMap[promotion]
            if (promotionConfig != null && promotionConfig.aliases.any { it == scopeToMatch }) return true
        }

        return false
    }

    fun matchesScope(scopeContext: ParadoxScopeContext?, scopesToMatch: Set<String>?, configGroup: CwtConfigGroup): Boolean {
        val thisScope = scopeContext?.scope?.id
        if (thisScope == null) return true
        if (scopesToMatch.isNullOrEmpty() || scopesToMatch == ParadoxScopeId.anyScopeIdSet) return true
        if (thisScope == ParadoxScopeId.anyScopeId) return true
        if (thisScope == ParadoxScopeId.unknownScopeId) return true
        if (thisScope in scopesToMatch) return true
        val scopeConfig = configGroup.scopeAliasMap[thisScope]
        if (scopeConfig != null) return scopeConfig.aliases.any { it in scopesToMatch }

        // from promotions
        val promotions = scopeContext.promotions
        for (promotion in promotions) {
            if (promotion in scopesToMatch) return true
            val promotionConfig = configGroup.scopeAliasMap[promotion]
            if (promotionConfig != null && promotionConfig.aliases.any { it in scopesToMatch }) return true
        }

        return false
    }

    fun matchesScopeGroup(scopeContext: ParadoxScopeContext?, scopeGroupToMatch: String, configGroup: CwtConfigGroup): Boolean {
        val thisScope = scopeContext?.scope?.id
        if (thisScope == null) return true
        if (thisScope == ParadoxScopeId.anyScopeId) return true
        if (thisScope == ParadoxScopeId.unknownScopeId) return true
        val scopeGroupConfig = configGroup.scopeGroups[scopeGroupToMatch] ?: return false
        for (scopeToMatch in scopeGroupConfig.values) {
            if (thisScope == scopeToMatch) return true
            val scopeConfig = configGroup.scopeAliasMap[thisScope]
            if (scopeConfig != null && scopeConfig.aliases.any { it == scopeToMatch }) return true
        }
        return false // cwt config error
    }
}
