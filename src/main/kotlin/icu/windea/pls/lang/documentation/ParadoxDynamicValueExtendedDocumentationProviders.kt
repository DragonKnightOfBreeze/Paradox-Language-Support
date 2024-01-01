package icu.windea.pls.lang.documentation

import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*

class ParadoxBaseDynamicValueExtendedDocumentationProvider : ParadoxDynamicValueExtendedDocumentationProvider {
    override fun getDocumentation(element: ParadoxDynamicValueElement): String? {
        if(element.name.isEmpty()) return null //ignore
        val configGroup = getConfigGroup(element.project, element.gameType)
        val dynamicValueConfig = configGroup.dynamicValues[element.dynamicValueType] ?:return null
        val config = dynamicValueConfig.valueConfigMap[element.name] ?: return null
        val documentation = config.documentation?.orNull()
        return documentation
    }
}