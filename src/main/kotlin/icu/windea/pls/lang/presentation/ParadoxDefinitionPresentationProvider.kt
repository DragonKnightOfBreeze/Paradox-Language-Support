package icu.windea.pls.lang.presentation

import com.intellij.openapi.extensions.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 用于绘制定义对应的UI表示。
 */
interface ParadoxDefinitionPresentationProvider {
    fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean
    
    fun getPresentation(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): JComponent?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxDefinitionPresentationProvider>("icu.windea.pls.definitionPresentationProvider")
        
        fun getPresentation(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): JComponent? {
            return EP_NAME.extensionList.firstNotNullOfOrNull t@{
                if(!it.supports(definition, definitionInfo)) return@t null   
                it.getPresentation(definition, definitionInfo)
            }
        }
    }
}

