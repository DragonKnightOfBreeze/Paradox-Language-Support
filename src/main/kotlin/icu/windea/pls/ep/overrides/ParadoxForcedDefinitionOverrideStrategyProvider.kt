package icu.windea.pls.ep.overrides

import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.data.types
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxSearchParameters
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxForcedDefinitionOverrideStrategyProvider : ParadoxOverrideStrategyProvider {
    override fun get(target: Any): ParadoxOverrideStrategy? {
        if (target !is ParadoxScriptProperty) return null
        val definitionInfo = target.definitionInfo ?: return null
        val typeConfig = definitionInfo.typeConfig
        return getOverrideStrategy(typeConfig)
    }

    override fun get(searchParameters: ParadoxSearchParameters<*>): ParadoxOverrideStrategy? {
        if (searchParameters !is ParadoxDefinitionSearch.SearchParameters) return null
        val definitionType = searchParameters.typeExpression?.substringBefore('.') ?: return null
        val gameType = searchParameters.selector.gameType ?: return null
        val configGroup = PlsFacade.getConfigGroup(searchParameters.project, gameType)
        val typeConfig = configGroup.types.get(definitionType) ?: return null
        return getOverrideStrategy(typeConfig)
    }

    private fun getOverrideStrategy(typeConfig: CwtTypeConfig): ParadoxOverrideStrategy? {
        return when {
            // event namespace -> ORDERED (don't care)
            typeConfig.name == ParadoxDefinitionTypes.EventNamespace -> ParadoxOverrideStrategy.ORDERED
            // swapped type -> ORDERED (don't care)
            typeConfig.baseType != null -> ParadoxOverrideStrategy.ORDERED
            // force anonymous -> ORDERED (don't care)
            typeConfig.nameField == "" -> ParadoxOverrideStrategy.ORDERED
            else -> null
        }
    }
}
