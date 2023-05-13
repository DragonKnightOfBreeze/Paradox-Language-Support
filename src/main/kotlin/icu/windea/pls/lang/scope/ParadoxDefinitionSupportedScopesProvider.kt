package icu.windea.pls.lang.scope

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

@WithGameTypeEP
interface ParadoxDefinitionSupportedScopesProvider {
    /**
     * 得到支持的作用域。
     */
    fun getSupportedScopes(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxDefinitionSupportedScopesProvider>("icu.windea.pls.definitionSupportedScopesProvider")
        
        fun getSupportedScopes(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensions.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.getSupportedScopes(definition, definitionInfo)
            }
        }
    }
}

