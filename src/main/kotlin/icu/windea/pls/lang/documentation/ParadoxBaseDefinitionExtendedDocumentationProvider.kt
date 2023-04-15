package icu.windea.pls.lang.documentation

import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

class ParadoxBaseDefinitionExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        val gameType = definitionInfo.gameType
        return ParadoxExtendedDocumentationBundle.message(gameType, definitionInfo.name, definitionInfo.type)
    }
}