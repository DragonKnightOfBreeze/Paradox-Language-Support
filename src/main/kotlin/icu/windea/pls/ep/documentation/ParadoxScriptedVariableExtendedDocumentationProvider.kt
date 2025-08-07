package icu.windea.pls.ep.documentation

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 用于为封装变量提供扩展的快速文档。
 */
@WithGameTypeEP
interface ParadoxScriptedVariableExtendedDocumentationProvider {
    fun getDocumentationContent(element: ParadoxScriptScriptedVariable): String?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxScriptedVariableExtendedDocumentationProvider>("icu.windea.pls.scriptedVariableExtendedDocumentationProvider")

        fun getDocumentationContent(element: ParadoxScriptScriptedVariable): String? {
            val gameType = selectGameType(element)
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                val content = ep.getDocumentationContent(element)?.orNull() ?: return@f null
                content.orNull()
            }
        }

        fun getAllDocumentationContent(element: ParadoxScriptScriptedVariable): List<String> {
            val gameType = selectGameType(element)
            return EP_NAME.extensionList.mapNotNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                val content = ep.getDocumentationContent(element)?.orNull() ?: return@f null
                content.orNull()
            }.optimized()
        }
    }
}
