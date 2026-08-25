package icu.windea.pls.lang.scope

import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.model.scope.ParadoxScopeConstants
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.scope.promotions

object ParadoxScopeMatchService {
    // NOTE 3.0.2 support to match union scopes (super scopes will not match sub scopes)

    @Suppress("unused")
    fun matchesScope(thisScope: String, scopeToMatch: String, configGroup: CwtConfigGroup): Boolean {
        // TODO 3.0.2 for any, all, union (e.g., carrier), and other normal scopes
        TODO()
    }

    fun matchesScope(scopeContext: ParadoxScopeContext?, scopeToMatch: String, configGroup: CwtConfigGroup): Boolean {
        val thisScope = scopeContext?.scope?.id
        if (thisScope == null) return true
        if (scopeToMatch == ParadoxScopeConstants.anyScope) return true
        if (thisScope == ParadoxScopeConstants.anyScope) return true
        if (thisScope == ParadoxScopeConstants.unknownScope) return true
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
        if (scopesToMatch.isNullOrEmpty() || scopesToMatch == ParadoxScopeConstants.anyScopes) return true
        if (thisScope == ParadoxScopeConstants.anyScope) return true
        if (thisScope == ParadoxScopeConstants.unknownScope) return true
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
        if (thisScope == ParadoxScopeConstants.anyScope) return true
        if (thisScope == ParadoxScopeConstants.unknownScope) return true
        val scopeGroupConfig = configGroup.scopeGroups[scopeGroupToMatch] ?: return false
        for (scopeToMatch in scopeGroupConfig.values) {
            if (thisScope == scopeToMatch) return true
            val scopeConfig = configGroup.scopeAliasMap[thisScope]
            if (scopeConfig != null && scopeConfig.aliases.any { it == scopeToMatch }) return true
        }
        return false // cwt config error
    }
}
