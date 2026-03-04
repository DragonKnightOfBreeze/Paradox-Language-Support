package icu.windea.pls.lang.resolve

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.script.psi.ParadoxScriptMember

object ParadoxEffectService {
    @Suppress("unused")
    fun isWithinEffectClause(element: ParadoxScriptMember): Boolean? {
        val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return null
        val parentConfigs = generateSequence(config) { it.parentConfig }
        return parentConfigs.any { it is CwtPropertyConfig && it.aliasConfig?.name == "effect" }
    }
}
