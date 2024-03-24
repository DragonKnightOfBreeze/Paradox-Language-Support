package icu.windea.pls.ep.documentation

import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*

class ParadoxBaseComplexEnumValueExtendedDocumentationProvider : ParadoxComplexEnumValueExtendedDocumentationProvider {
    override fun getDocumentation(element: ParadoxComplexEnumValueElement): String? {
        //TODO 1.3.4
        //if(element.name.isEmpty()) return null //ignore
        //val configGroup = getConfigGroup(element.project, element.gameType)
        //val dynamicValueTypeConfig = configGroup.dynamicValueTypes[element.enumName] ?:return null
        //val config = dynamicValueTypeConfig.valueConfigMap[element.name] ?: return null
        //val documentation = config.documentation?.orNull()
        //return documentation
        return null
    }
}