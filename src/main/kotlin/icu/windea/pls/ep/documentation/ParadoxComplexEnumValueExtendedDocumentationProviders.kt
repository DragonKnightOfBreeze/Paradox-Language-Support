package icu.windea.pls.ep.documentation

import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*

class ParadoxBaseComplexEnumValueExtendedDocumentationProvider : ParadoxComplexEnumValueExtendedDocumentationProvider {
    override fun getDocumentation(element: ParadoxComplexEnumValueElement): String? {
        val name = element.name
        if(name.isEmpty()) return null
        if(name.isParameterized()) return null
        val configGroup = getConfigGroup(element.project, element.gameType)
        val configs = configGroup.extendedComplexEnumValues[element.enumName] ?: return null
        val config = configs.getByTemplate(name, element, configGroup) ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}