package icu.windea.pls.lang.presentation

import com.intellij.openapi.extensions.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.awt.*

/**
 * 用户绘制定义对应的UI表示。例如，科技卡图片。
 */
interface ParadoxDefinitionPresentationProvider {
    fun getPresentation(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Image?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxDefinitionPresentationProvider>("icu.windea.pls.definitionPresentationProvider")
        
        fun getPresentation(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Image? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.getPresentation(definition, definitionInfo) }
        }
    }
}

