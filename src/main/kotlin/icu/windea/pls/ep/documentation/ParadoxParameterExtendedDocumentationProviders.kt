package icu.windea.pls.ep.documentation

import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*

class ParadoxBaseParameterExtendedDocumentationProvider : ParadoxParameterExtendedDocumentationProvider {
    override fun getDocumentationContent(element: ParadoxParameterElement): String? {
        val name = element.name
        if (name.isEmpty()) return null
        if (name.isParameterized()) return null
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        val configs = configGroup.extendedParameters.findFromPattern(name, element, configGroup).orEmpty()
        val config = configs.findLast { it.contextKey.matchFromPattern(element.contextKey, element, configGroup) } ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}
