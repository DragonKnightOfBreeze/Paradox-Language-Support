package icu.windea.pls.lang.scope

import com.intellij.openapi.extensions.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

interface ParadoxDefinitionScopeContextProvider {
    /**
     * 得到作用域。
     * @return 得到的作用域上下文信息。
     */
    fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxDefinitionScopeContextProvider>("icu.windea.pls.definitionScopeContextProvider")
        
        @JvmStatic
        fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
            for(extension in EP_NAME.extensions) {
                val scopeContext = extension.getScopeContext(definition, definitionInfo)
                if(scopeContext != null) return scopeContext
            }
            return null
        }
    }
}