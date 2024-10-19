package icu.windea.pls.ep.scope

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.model.*

class ParadoxBaseDynamicValueScopeContextProvider : ParadoxDynamicValueScopeContextProvider {
    override fun supports(element: ParadoxDynamicValueElement): Boolean {
        return true
    }

    override fun getScopeContext(element: ParadoxDynamicValueElement): ParadoxScopeContext? {
        val name = element.name
        val types = element.dynamicValueTypes
        val configGroup = getConfigGroup(element.project, element.gameType)
        for (type in types) {
            val configs = configGroup.extendedDynamicValues[type] ?: continue
            val config = configs.findFromPattern(name, element, configGroup) ?: continue
            val result = config.config.scopeContext ?: continue
            return result
        }
        return null
    }
}
