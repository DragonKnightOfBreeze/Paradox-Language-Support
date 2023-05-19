package icu.windea.pls.lang.documentation

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于为定义提供扩展的快速文档文本。
 */
@WithGameTypeEP
interface ParadoxDefinitionExtendedDocumentationProvider {
    fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxDefinitionExtendedDocumentationProvider>("icu.windea.pls.definitionExtendedDocumentationProvider")
        
        fun buildDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, action: (String) -> Unit) {
            val gameType = definitionInfo.gameType
            EP_NAME.extensionList.forEachFast f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f
                val documentation = ep.getDocumentation(definition, definitionInfo)?.takeIfNotEmpty()
                if(documentation != null) {
                    action(documentation)
                }
            }
        }
    }
}
