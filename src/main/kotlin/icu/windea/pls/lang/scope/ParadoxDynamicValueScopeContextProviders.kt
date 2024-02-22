package icu.windea.pls.lang.scope

import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.model.*

class ParadoxBaseDynamicValueScopeContextProvider: ParadoxDynamicValueScopeContextProvider {
    override fun supports(element: ParadoxDynamicValueElement): Boolean {
        return true
    }
    
    override fun getScopeContext(element: ParadoxDynamicValueElement): ParadoxScopeContext? {
        val configGroup = getConfigGroup(element.project, element.gameType)
        val dynamicValueTypeConfig = configGroup.dynamicValueTypes[element.dynamicValueType] ?:return null
        val config = dynamicValueTypeConfig.valueConfigMap[element.name] ?: return null
        val result = config.scopeContext
        return result
    }
}
