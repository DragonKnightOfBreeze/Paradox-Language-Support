package icu.windea.pls.lang.scope

import com.intellij.openapi.extensions.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

interface ParadoxDefinitionSupportedScopesProvider {
    /**
     * 得到支持的作用域。
     */
    fun getSupportedScopes(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxDefinitionSupportedScopesProvider>("icu.windea.pls.definitionSupportedScopesProvider")
        
        fun getSupportedScopes(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>? {
            for(extension in EP_NAME.extensions) {
                val supportedScopes = extension.getSupportedScopes(definition, definitionInfo)
                if(supportedScopes != null) return supportedScopes
            }
            return null
        }
    }
}

