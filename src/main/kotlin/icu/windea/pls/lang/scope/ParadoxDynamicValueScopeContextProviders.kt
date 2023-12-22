package icu.windea.pls.lang.scope

import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.model.*

class ParadoxBaseDynamicValueScopeContextProvider: ParadoxDynamicValueScopeContextProvider {
    override fun supports(element: ParadoxValueSetValueElement): Boolean {
        return true
    }
    
    override fun getScopeContext(element: ParadoxValueSetValueElement): ParadoxScopeContext? {
        val configGroup = getConfigGroup(element.project, element.gameType)
        val dynamicValueConfig = configGroup.dynamicValues[element.valueSetName] ?:return null
        val config = dynamicValueConfig.valueConfigMap[element.name] ?: return null
        val result = config.scopeContext
        return result
    }
}
