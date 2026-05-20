package icu.windea.pls.ep.overrides

import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.lang.search.ParadoxDefinitionInjectionSearch
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxForcedDefinitionInjectionOverrideStrategyProvider : ParadoxOverrideStrategyProvider {
    override fun get(target: Any): ParadoxOverrideStrategy? {
        if (target !is ParadoxScriptProperty) return null
        if (target.definitionInjectionInfo == null) return null
        return getOverrideStrategy()
    }

    override fun get(searchParameters: ParadoxSearchParameters<*>): ParadoxOverrideStrategy? {
        if (searchParameters !is ParadoxDefinitionInjectionSearch.Parameters) return null
        return getOverrideStrategy()
    }

    private fun getOverrideStrategy(): ParadoxOverrideStrategy {
        return ParadoxOverrideStrategy.LIOS
    }
}
