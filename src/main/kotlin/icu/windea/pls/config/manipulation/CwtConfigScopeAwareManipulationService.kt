package icu.windea.pls.config.manipulation

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.parents

@Suppress("unused")
object CwtConfigScopeAwareManipulationService {
     fun isTriggerClause(config: CwtMemberConfig<*>): Boolean {
         if(config !is CwtPropertyConfig) return false
         val aliasConfig = config.aliasConfig ?: return false
         return aliasConfig.name == "trigger"
    }

    fun isEffectClause(config: CwtMemberConfig<*>): Boolean {
        if(config !is CwtPropertyConfig) return false
        val aliasConfig = config.aliasConfig ?: return false
        return aliasConfig.name == "effect"
    }

    fun isTriggerOrEffectClause(config: CwtMemberConfig<*>): Boolean {
        if(config !is CwtPropertyConfig) return false
        val aliasConfig = config.aliasConfig ?: return false
        return aliasConfig.name == "trigger" || aliasConfig.name == "effect"
    }

     fun withinTriggerClause(config: CwtMemberConfig<*>, withSelf: Boolean = false): Boolean {
        return config.parents(withSelf).any { isTriggerClause(it) }
    }

     fun withinEffectClause(config: CwtMemberConfig<*>, withSelf: Boolean = false): Boolean {
        return config.parents(withSelf).any { isEffectClause(it) }
    }

    fun withinTriggerOrEffectClause(config: CwtMemberConfig<*>, withSelf: Boolean = false): Boolean {
        return config.parents(withSelf).any { isTriggerOrEffectClause(it) }
    }
}
