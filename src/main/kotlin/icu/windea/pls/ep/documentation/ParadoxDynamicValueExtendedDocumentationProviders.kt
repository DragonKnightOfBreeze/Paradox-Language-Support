package icu.windea.pls.ep.documentation

import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*

class ParadoxBaseDynamicValueExtendedDocumentationProvider : ParadoxDynamicValueExtendedDocumentationProvider {
    override fun getDocumentation(element: ParadoxDynamicValueElement): String? {
        if(element.name.isEmpty()) return null //ignore
        val configGroup = getConfigGroup(element.project, element.gameType)
        val dynamicValueTypeConfig = configGroup.dynamicValueTypes[element.dynamicValueType] ?:return null
        val config = dynamicValueTypeConfig.valueConfigMap[element.name] ?: return null
        val documentation = config.documentation?.orNull()
        return documentation
    }
}