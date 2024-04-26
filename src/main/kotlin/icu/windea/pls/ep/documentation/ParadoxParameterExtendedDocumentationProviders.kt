package icu.windea.pls.ep.documentation

import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*

class ParadoxBaseParameterExtendedDocumentationProvider : ParadoxParameterExtendedDocumentationProvider {
    override fun getDocumentation(element: ParadoxParameterElement): String? {
        val name = element.name
        if(name.isEmpty()) return null
        if(name.isParameterized()) return null
        val configGroup = getConfigGroup(element.project, element.gameType)
        val configs = configGroup.extendedParameters.findByPattern(name, element, configGroup).orEmpty()
        val config = configs.findLast { it.contextKey == element.contextKey } ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}