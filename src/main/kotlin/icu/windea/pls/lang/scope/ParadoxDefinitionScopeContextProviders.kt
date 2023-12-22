package icu.windea.pls.lang.scope

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxDefaultDefinitionScopeContextProvider: ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return true
    }
    
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

class ParadoxBaseDefinitionScopeContextProvider: ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return true
    }
    
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val configGroup = definitionInfo.configGroup
        val configs = configGroup.definitions.getAllByTemplate(definitionInfo.name, definition, configGroup)
        val config = configs.findLast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) } ?: return null
        val result = config?.config?.scopeContext
        return result
    }
}

class ParadoxGameRuleScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "game_rule"
    }
    
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val configGroup = definitionInfo.configGroup
        val config = configGroup.gameRules.getByTemplate(definitionInfo.name, definition, configGroup)
        val result = config?.config?.scopeContext
        return result
    }
}

class ParadoxOnActionScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "on_action"
    }
    
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val configGroup = definitionInfo.configGroup
        val config = configGroup.onActions.getByTemplate(definitionInfo.name, definition, configGroup)
        val result = config?.config?.scopeContext
        return result
    }
}