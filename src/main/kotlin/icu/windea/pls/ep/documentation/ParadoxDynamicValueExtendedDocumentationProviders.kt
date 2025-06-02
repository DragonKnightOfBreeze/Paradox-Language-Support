package icu.windea.pls.ep.documentation

import icu.windea.pls.PlsFacade
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*

class ParadoxBaseDynamicValueExtendedDocumentationProvider : ParadoxDynamicValueExtendedDocumentationProvider {
    override fun getDocumentationContent(element: ParadoxDynamicValueElement): String? {
        val name = element.name
        if (name.isEmpty()) return null
        if (name.isParameterized()) return null
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        for (type in element.dynamicValueTypes) {
            val configs = configGroup.extendedDynamicValues[type] ?: continue
            val config = configs.findFromPattern(name, element, configGroup) ?: continue
            val documentation = config.config.documentation?.orNull()
            if (documentation != null) return documentation
        }
        return null
    }
}
