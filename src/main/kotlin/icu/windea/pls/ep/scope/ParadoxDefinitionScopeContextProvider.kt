package icu.windea.pls.ep.scope

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于为定义提供作用域上下文。
 */
@WithGameTypeEP
interface ParadoxDefinitionScopeContextProvider {
    fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean

    fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxDefinitionScopeContextProvider>("icu.windea.pls.definitionScopeContextProvider")

        fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                if (!ep.supports(definition, definitionInfo)) return@f null
                ep.getScopeContext(definition, definitionInfo)
            }
        }
    }
}

