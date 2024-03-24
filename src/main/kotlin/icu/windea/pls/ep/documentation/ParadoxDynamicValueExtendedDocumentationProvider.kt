package icu.windea.pls.ep.documentation

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.psi.*

/**
 * 用于为动态值提供扩展的快速文档。
 */
@WithGameTypeEP
interface ParadoxDynamicValueExtendedDocumentationProvider {
    fun getDocumentation(element: ParadoxDynamicValueElement): String?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxDynamicValueExtendedDocumentationProvider>("icu.windea.pls.dynamicValueExtendedDocumentationProvider")
        
        fun buildDocumentation(element: ParadoxDynamicValueElement, action: (String) -> Unit) {
            val gameType = element.gameType
            EP_NAME.extensionList.forEachFast f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f
                val documentation = ep.getDocumentation(element)?.orNull()
                if(documentation != null) {
                    action(documentation)
                }
            }
        }
    }
}