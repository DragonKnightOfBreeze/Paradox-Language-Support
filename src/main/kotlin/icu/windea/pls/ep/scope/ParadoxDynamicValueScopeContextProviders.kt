package icu.windea.pls.ep.scope

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.model.*

class ParadoxBaseDynamicValueScopeContextProvider: ParadoxDynamicValueScopeContextProvider {
    override fun supports(element: ParadoxDynamicValueElement): Boolean {
        return true
    }
    
    override fun getScopeContext(element: ParadoxDynamicValueElement): ParadoxScopeContext? {
        val name = element.name
        val type = element.dynamicValueType
        val configGroup = getConfigGroup(element.project, element.gameType)
        val configs = configGroup.extendedDynamicValues[type] ?: return null
        val config = configs.findFromPattern(name, element, configGroup) ?: return null
        val result = config.config.scopeContext
        return result
    }
}
