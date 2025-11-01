package icu.windea.pls.ep.scope

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 用于为定义提供作用域上下文。
 */
@WithGameTypeEP
interface ParadoxDefinitionScopeContextProvider {
    fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean

    fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionScopeContextProvider>("icu.windea.pls.definitionScopeContextProvider")

        fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f null
                if (!ep.supports(definition, definitionInfo)) return@f null
                ep.getScopeContext(definition, definitionInfo)
            }
        }
    }
}

