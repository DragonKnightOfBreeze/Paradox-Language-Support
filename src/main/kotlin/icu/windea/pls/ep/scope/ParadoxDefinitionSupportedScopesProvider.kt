package icu.windea.pls.ep.scope

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

@WithGameTypeEP
interface ParadoxDefinitionSupportedScopesProvider {
    fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean
    
    /**
     * 得到支持的作用域。
     */
    fun getSupportedScopes(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxDefinitionSupportedScopesProvider>("icu.windea.pls.definitionSupportedScopesProvider")
        
        fun getSupportedScopes(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                if(!ep.supports(definition, definitionInfo)) return@f null
                ep.getSupportedScopes(definition, definitionInfo)
            }
        }
    }
}
