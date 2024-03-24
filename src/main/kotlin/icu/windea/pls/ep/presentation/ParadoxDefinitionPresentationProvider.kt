package icu.windea.pls.ep.presentation

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.extensions.*
import com.intellij.openapi.progress.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 用于绘制定义的UI表示。
 */
@WithGameTypeEP
interface ParadoxDefinitionPresentationProvider {
    fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean
    
    fun getPresentation(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): JComponent?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxDefinitionPresentationProvider>("icu.windea.pls.definitionPresentationProvider")
        
        fun getPresentation(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): JComponent? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                if(!ep.supports(definition, definitionInfo)) return@f null
                try {
                    ep.getPresentation(definition, definitionInfo)
                } catch(e: Exception) {
                    if(e is ProcessCanceledException) throw e
                    thisLogger().warn(e)
                    null
                }
            }
        }
    }
}

