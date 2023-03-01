package icu.windea.pls.lang.documentation

import icu.windea.pls.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

class ParadoxBaseDefinitionExtendedDocumentationProvider: ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        //尝试从PlsExtDocBundle.properties中获取
        return PlsExtDocBundle.message(definitionInfo.name, definitionInfo.type, definitionInfo.gameType)
    }
}