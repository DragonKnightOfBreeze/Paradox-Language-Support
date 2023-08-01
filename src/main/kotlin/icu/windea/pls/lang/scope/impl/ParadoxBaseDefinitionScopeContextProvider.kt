package icu.windea.pls.lang.scope.impl

import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*
import icu.windea.pls.lang.scope.*
import icu.windea.pls.script.psi.*

class ParadoxBaseDefinitionScopeContextProvider: ParadoxDefinitionScopeContextProvider {
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val declarationConfig = definitionInfo.declarationConfig?.propertyConfig ?: return null
        val subtypeConfigs = definitionInfo.subtypeConfigs
        val typeConfig = definitionInfo.typeConfig
        val scopeContextOnType = subtypeConfigs.firstNotNullOfOrNull { it.config.scopeContext }
            ?: typeConfig.config.scopeContext
        val scopeContext = scopeContextOnType
            ?: declarationConfig.scopeContext
        val pushScopeOnType = (subtypeConfigs.firstNotNullOfOrNull { it.config.pushScope }
            ?: typeConfig.config.pushScope)
        val pushScope = pushScopeOnType
            ?: declarationConfig.pushScope
        val result = scopeContext?.resolve(pushScope)
            ?: pushScope?.let { ParadoxScopeContext.resolve(it, it) }
        return result
    }
}