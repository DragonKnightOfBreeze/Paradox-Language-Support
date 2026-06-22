package icu.windea.pls.lang.manipulation

import icu.windea.pls.config.manipulation.CwtConfigScopeAwareManipulationService
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.script.psi.ParadoxScriptMember

@Suppress("unused")
object ParadoxScopeAwareManipulationService {
    fun withinTriggerClause(element: ParadoxScriptMember): Boolean? {
        val configs = ParadoxConfigManager.getConfigs(element)
        if (configs.isEmpty()) return null
        return configs.any { config -> CwtConfigScopeAwareManipulationService.withinTriggerClause(config) }
    }

    fun withinEffectClause(element: ParadoxScriptMember): Boolean? {
        val configs = ParadoxConfigManager.getConfigs(element)
        if (configs.isEmpty()) return null
        return configs.any { config -> CwtConfigScopeAwareManipulationService.withinEffectClause(config) }
    }

    fun withinTriggerOrEffectClause(element: ParadoxScriptMember): Boolean? {
        val configs = ParadoxConfigManager.getConfigs(element)
        if (configs.isEmpty()) return null
        return configs.any { config -> CwtConfigScopeAwareManipulationService.withinTriggerOrEffectClause(config) }
    }
}
