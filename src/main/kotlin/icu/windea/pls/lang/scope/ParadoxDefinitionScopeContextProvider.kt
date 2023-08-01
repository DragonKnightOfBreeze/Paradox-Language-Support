package icu.windea.pls.lang.scope

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

@WithGameTypeEP
interface ParadoxDefinitionScopeContextProvider {
    /**
     * 得到作用域。
     * @return 得到的作用域上下文信息。
     */
    fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxDefinitionScopeContextProvider>("icu.windea.pls.definitionScopeContextProvider")
        
        fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.getScopeContext(definition, definitionInfo)
            }
        }
    }
}

