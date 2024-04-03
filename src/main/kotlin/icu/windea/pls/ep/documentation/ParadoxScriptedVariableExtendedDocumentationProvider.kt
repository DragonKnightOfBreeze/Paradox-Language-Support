package icu.windea.pls.ep.documentation

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.psi.*

/**
 * 用于为封装变量提供扩展的快速文档。
 */
@WithGameTypeEP
interface ParadoxScriptedVariableExtendedDocumentationProvider {
    fun getDocumentation(element: ParadoxScriptScriptedVariable): String?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxScriptedVariableExtendedDocumentationProvider>("icu.windea.pls.scriptedVariableExtendedDocumentationProvider")
        
        fun buildDocumentation(element: ParadoxScriptScriptedVariable, action: (String) -> Unit) {
            val gameType = selectGameType(element)
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