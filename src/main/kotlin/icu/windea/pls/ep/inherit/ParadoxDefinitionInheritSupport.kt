package icu.windea.pls.ep.inherit

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于实现定义继承的逻辑。
 */
@WithGameTypeEP
interface ParadoxDefinitionInheritSupport {
    fun getSuperDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScriptDefinitionElement?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxDefinitionInheritSupport>("icu.windea.pls.definitionInheritSupport")
        
        fun getSuperDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScriptDefinitionElement? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.getSuperDefinition(definition, definitionInfo)
            }
        }
    }
}