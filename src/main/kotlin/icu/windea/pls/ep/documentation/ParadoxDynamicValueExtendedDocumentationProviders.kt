package icu.windea.pls.ep.documentation

import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*

class ParadoxBaseDynamicValueExtendedDocumentationProvider : ParadoxDynamicValueExtendedDocumentationProvider {
    override fun getDocumentation(element: ParadoxDynamicValueElement): String? {
        val name = element.name
        if(name.isEmpty()) return null
        if(name.isParameterized()) return null
        val configGroup = getConfigGroup(element.project, element.gameType)
        val configs = configGroup.extendedDynamicValues[element.dynamicValueType] ?:return null
        val config = configs.findFromPattern(name, element, configGroup) ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}
