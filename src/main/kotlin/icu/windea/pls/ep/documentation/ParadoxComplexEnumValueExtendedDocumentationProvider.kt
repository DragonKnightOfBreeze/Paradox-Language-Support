package icu.windea.pls.ep.documentation

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*

/**
 * 用于为复杂枚举值提供扩展的快速文档。
 */
@WithGameTypeEP
interface ParadoxComplexEnumValueExtendedDocumentationProvider {
    fun getDocumentationContent(element: ParadoxComplexEnumValueElement): String?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxComplexEnumValueExtendedDocumentationProvider>("icu.windea.pls.complexEnumValueExtendedDocumentationProvider")
        
        fun buildDocumentationContent(element: ParadoxComplexEnumValueElement, action: (String) -> Unit) {
            val gameType = element.gameType
            EP_NAME.extensionList.forEachFast f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f
                val content = ep.getDocumentationContent(element)?.orNull() ?: return@f
                action(content)
            }
        }
    }
}
