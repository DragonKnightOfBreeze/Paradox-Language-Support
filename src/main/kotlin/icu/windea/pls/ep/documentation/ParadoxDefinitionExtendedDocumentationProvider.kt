package icu.windea.pls.ep.documentation

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于为定义提供扩展的快速文档。
 */
@WithGameTypeEP
interface ParadoxDefinitionExtendedDocumentationProvider {
    fun getDocumentationContent(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionExtendedDocumentationProvider>("icu.windea.pls.definitionExtendedDocumentationProvider")

        fun getDocumentationContent(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                val content = ep.getDocumentationContent(definition, definitionInfo)?.orNull() ?: return@f null
                content.orNull()
            }
        }

        fun getAllDocumentationContent(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): List<String> {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.mapNotNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                val content = ep.getDocumentationContent(definition, definitionInfo)?.orNull() ?: return@f null
                content.orNull()
            }.optimized()
        }
    }
}
