package icu.windea.pls.ep.documentation

import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*

class ParadoxBaseComplexEnumValueExtendedDocumentationProvider : ParadoxComplexEnumValueExtendedDocumentationProvider {
    override fun getDocumentation(element: ParadoxComplexEnumValueElement): String? {
        if(element.name.isEmpty()) return null //ignore
        val configGroup = getConfigGroup(element.project, element.gameType)
        val configs = configGroup.dynamicValues[element.enumName] ?: return null
        val config = configs[element.name] ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}