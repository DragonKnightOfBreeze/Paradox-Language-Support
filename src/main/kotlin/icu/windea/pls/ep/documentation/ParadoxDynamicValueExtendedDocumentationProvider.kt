package icu.windea.pls.ep.documentation

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*

/**
 * 用于为动态值提供扩展的快速文档。
 */
@WithGameTypeEP
interface ParadoxDynamicValueExtendedDocumentationProvider {
    fun getDocumentationContent(element: ParadoxDynamicValueElement): String?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxDynamicValueExtendedDocumentationProvider>("icu.windea.pls.dynamicValueExtendedDocumentationProvider")

        fun getDocumentationContent(element: ParadoxDynamicValueElement): String? {
            val gameType = element.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                val content = ep.getDocumentationContent(element)?.orNull() ?: return@f null
                content.orNull()
            }
        }

        fun getAllDocumentationContent(element: ParadoxDynamicValueElement): List<String> {
            val gameType = element.gameType
            return EP_NAME.extensionList.mapNotNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                val content = ep.getDocumentationContent(element)?.orNull() ?: return@f null
                content.orNull()
            }.optimized()
        }
    }
}
