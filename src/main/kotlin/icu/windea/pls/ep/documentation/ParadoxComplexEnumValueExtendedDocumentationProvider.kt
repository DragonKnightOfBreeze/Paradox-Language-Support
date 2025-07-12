package icu.windea.pls.ep.documentation

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement

/**
 * 用于为复杂枚举值提供扩展的快速文档。
 */
@WithGameTypeEP
interface ParadoxComplexEnumValueExtendedDocumentationProvider {
    fun getDocumentationContent(element: ParadoxComplexEnumValueElement): String?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxComplexEnumValueExtendedDocumentationProvider>("icu.windea.pls.complexEnumValueExtendedDocumentationProvider")

        fun getDocumentationContent(element: ParadoxComplexEnumValueElement): String? {
            val gameType = element.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                val content = ep.getDocumentationContent(element)?.orNull() ?: return@f null
                content.orNull()
            }
        }

        fun getAllDocumentationContent(element: ParadoxComplexEnumValueElement): List<String> {
            val gameType = element.gameType
            return EP_NAME.extensionList.mapNotNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                val content = ep.getDocumentationContent(element)?.orNull() ?: return@f null
                content.orNull()
            }.optimized()
        }
    }
}
