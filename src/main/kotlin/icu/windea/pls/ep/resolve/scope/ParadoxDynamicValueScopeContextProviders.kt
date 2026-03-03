package icu.windea.pls.ep.resolve.scope

import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.model.scope.ParadoxScopeContext

class ParadoxBaseDynamicValueScopeContextProvider : ParadoxDynamicValueScopeContextProvider {
    override fun supports(element: ParadoxDynamicValueLightElement): Boolean {
        return true
    }

    override fun getScopeContext(element: ParadoxDynamicValueLightElement): ParadoxScopeContext? {
        val name = element.name
        val types = element.dynamicValueTypes
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        for (type in types) {
            val configs = configGroup.extendedDynamicValues[type] ?: continue
            val config = configs.findByPattern(name, element, configGroup) ?: continue
            val result = config.config.optionData.scopeContext ?: continue
            return result
        }
        return null
    }
}
