package icu.windea.pls.config.manipulation

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.parents

@Suppress("unused")
object CwtConfigScopeAwareManipulationService {
    fun isTrigger(config: CwtMemberConfig<*>): Boolean {
        if (config !is CwtPropertyConfig) return false
        val aliasConfig = config.aliasConfig ?: return false
        return aliasConfig.name == "trigger"
    }

    fun isEffect(config: CwtMemberConfig<*>): Boolean {
        if (config !is CwtPropertyConfig) return false
        val aliasConfig = config.aliasConfig ?: return false
        return aliasConfig.name == "effect"
    }

    fun isTriggerOrEffect(config: CwtMemberConfig<*>): Boolean {
        if (config !is CwtPropertyConfig) return false
        val aliasConfig = config.aliasConfig ?: return false
        return aliasConfig.name == "trigger" || aliasConfig.name == "effect"
    }

    fun withinTriggerClause(config: CwtMemberConfig<*>): Boolean {
        return config.parents(withSelf = true).any { isTrigger(it) }
    }

    fun withinEffectClause(config: CwtMemberConfig<*>): Boolean {
        return config.parents(withSelf = true).any { isEffect(it) }
    }

    fun withinTriggerOrEffectClause(config: CwtMemberConfig<*>): Boolean {
        return config.parents(withSelf = true).any { isTriggerOrEffect(it) }
    }
}
