package icu.windea.pls.ep.documentation

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.psi.*

/**
 * 用于为复杂枚举值提供扩展的快速文档。
 */
@WithGameTypeEP
interface ParadoxComplexEnumValueExtendedDocumentationProvider {
    fun getDocumentation(element: ParadoxComplexEnumValueElement): String?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxComplexEnumValueExtendedDocumentationProvider>("icu.windea.pls.complexEnumValueExtendedDocumentationProvider")
        
        fun buildDocumentation(element: ParadoxComplexEnumValueElement, action: (String) -> Unit) {
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
