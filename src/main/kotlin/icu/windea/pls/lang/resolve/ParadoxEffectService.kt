package icu.windea.pls.lang.resolve

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.script.psi.ParadoxScriptMember

@Suppress("unused")
object ParadoxEffectService {
    fun isWithinEffectClause(element: ParadoxScriptMember): Boolean? {
        val configs = ParadoxConfigManager.getConfigs(element)
        if (configs.isEmpty()) return null
        return configs.any { config ->
            val parentConfigs = generateSequence(config) { it.parentConfig }
            parentConfigs.any { it is CwtPropertyConfig && it.aliasConfig?.name == "effect" }
        }
    }
}
