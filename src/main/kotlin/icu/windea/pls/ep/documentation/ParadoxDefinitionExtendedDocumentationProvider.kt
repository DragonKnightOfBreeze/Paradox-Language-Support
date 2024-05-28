package icu.windea.pls.ep.documentation

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于为定义提供扩展的快速文档。
 */
@WithGameTypeEP
interface ParadoxDefinitionExtendedDocumentationProvider {
    fun getDocumentationContent(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxDefinitionExtendedDocumentationProvider>("icu.windea.pls.definitionExtendedDocumentationProvider")
        
        fun buildDocumentationContent(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, action: (String) -> Unit) {
            val gameType = definitionInfo.gameType
            EP_NAME.extensionList.forEachFast f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f
                val content = ep.getDocumentationContent(definition, definitionInfo)?.orNull() ?: return@f
                action(content)
            }
        }
    }
}
