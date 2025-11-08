package icu.windea.pls.ep.resolve.scope

import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.configGroup.extendedDynamicValues
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.model.scope.ParadoxScopeContext

class ParadoxBaseDynamicValueScopeContextProvider : ParadoxDynamicValueScopeContextProvider {
    override fun supports(element: ParadoxDynamicValueElement): Boolean {
        return true
    }

    override fun getScopeContext(element: ParadoxDynamicValueElement): ParadoxScopeContext? {
        val name = element.name
        val types = element.dynamicValueTypes
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        for (type in types) {
            val configs = configGroup.extendedDynamicValues[type] ?: continue
            val config = configs.findByPattern(name, element, configGroup) ?: continue
            val result = config.config.optionData { scopeContext } ?: continue
            return result
        }
        return null
    }
}
