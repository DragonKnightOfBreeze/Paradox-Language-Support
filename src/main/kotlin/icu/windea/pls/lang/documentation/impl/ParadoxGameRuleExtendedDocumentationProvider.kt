package icu.windea.pls.lang.documentation.impl

import icu.windea.pls.core.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxGameRuleExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        //如果是game_rule，尝试从game_rules.cwt中得到对应的文档文本
        if(definitionInfo.type == "game_rule") {
            val config = definitionInfo.configGroup.gameRules.get(definitionInfo.name)
            val comment = config?.config?.documentation?.takeIfNotEmpty()
            return comment
        }
        return null
    }
}