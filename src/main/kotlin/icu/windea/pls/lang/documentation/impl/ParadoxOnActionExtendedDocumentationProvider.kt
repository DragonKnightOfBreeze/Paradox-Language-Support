package icu.windea.pls.lang.documentation.impl

import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxOnActionExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        //如果是on_action，尝试从on_actions.cwt中得到对应的文档文本
        if(definitionInfo.type == "on_action") {
            val config = definitionInfo.configGroup.onActions.getByTemplate(definitionInfo.name, definition, definitionInfo.configGroup)
            val comment = config?.config?.documentation?.orNull()
            return comment
        }
        return null
    }
}
