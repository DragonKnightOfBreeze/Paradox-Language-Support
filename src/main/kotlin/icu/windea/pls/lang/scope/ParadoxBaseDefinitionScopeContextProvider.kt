package icu.windea.pls.lang.scope

import icu.windea.pls.config.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

class ParadoxBaseDefinitionScopeContextProvider: ParadoxDefinitionScopeContextProvider {
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val declarationConfig = definitionInfo.declarationConfig?.propertyConfig ?: return null
        val subtypeConfigs = definitionInfo.subtypeConfigs
        val typeConfig = definitionInfo.typeConfig
        val replaceScopeOnType = subtypeConfigs.firstNotNullOfOrNull { it.config.replaceScopes }
            ?: typeConfig.config.replaceScopes
        val replaceScope = replaceScopeOnType
            ?: declarationConfig.replaceScopes
        val pushScopeOnType = (subtypeConfigs.firstNotNullOfOrNull { it.config.pushScope }
            ?: typeConfig.config.pushScope)
        val pushScope = pushScopeOnType
            ?: declarationConfig.pushScope
        val result = replaceScope?.resolve(pushScope)
            ?: pushScope?.let { ParadoxScopeContext.resolve(it, it) }
        return result
    }
}