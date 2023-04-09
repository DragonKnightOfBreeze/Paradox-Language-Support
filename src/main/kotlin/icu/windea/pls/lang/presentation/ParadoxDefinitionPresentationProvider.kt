package icu.windea.pls.lang.presentation

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.extensions.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 用于绘制定义的UI表示。
 */
interface ParadoxDefinitionPresentationProvider {
    fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean
    
    fun getPresentation(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): JComponent?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxDefinitionPresentationProvider>("icu.windea.pls.definitionPresentationProvider")
        
        fun getPresentation(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): JComponent? {
            return EP_NAME.extensionList.firstNotNullOfOrNull p@{
                if(!it.supports(definition, definitionInfo)) return@p null
                try {
                    it.getPresentation(definition, definitionInfo)
                } catch(e: Exception) {
                    thisLogger().warn(e)
                    null
                }
            }
        }
    }
}

