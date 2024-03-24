package icu.windea.pls.ep.documentation

import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*

class ParadoxBaseParameterExtendedDocumentationProvider : ParadoxParameterExtendedDocumentationProvider {
    override fun getDocumentation(element: ParadoxParameterElement): String? {
        if(element.name.isEmpty()) return null //ignore
        val configGroup = getConfigGroup(element.project, element.gameType)
        val configs = configGroup.parameters.getAllByTemplate(element.name, element, configGroup)
        val config = configs.findLast { it.contextKey == element.contextKey } ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}