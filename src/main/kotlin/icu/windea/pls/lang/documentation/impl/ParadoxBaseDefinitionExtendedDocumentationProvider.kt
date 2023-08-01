package icu.windea.pls.lang.documentation.impl

import icu.windea.pls.lang.documentation.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxBaseDefinitionExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
        val gameType = definitionInfo.gameType
        return ParadoxExtendedDocumentationBundle.message(gameType, definitionInfo.name, definitionInfo.type)
    }
}