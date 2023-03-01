package icu.windea.pls.lang.documentation

import com.intellij.openapi.extensions.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于为定义提供扩展的快速文档文本。
 */
interface ParadoxDefinitionExtendedDocumentationProvider {
    fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxDefinitionExtendedDocumentationProvider>("icu.windea.pls.definitionExtendedDocumentationProvider")
    }
}
