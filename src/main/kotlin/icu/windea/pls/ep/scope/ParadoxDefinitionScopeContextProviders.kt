package icu.windea.pls.ep.scope

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.script.psi.*

class ParadoxDefaultDefinitionScopeContextProvider: ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return true
    }
    
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val declarationConfig = definitionInfo.declarationConfig?.config ?: return null
        val subtypeConfigs = definitionInfo.subtypeConfigs
        val typeConfig = definitionInfo.typeConfig
        val scopeContextOnType = subtypeConfigs.firstNotNullOfOrNull { it.config.scopeContext }
            ?: typeConfig.config.scopeContext
        val scopeContextOnDeclaration = declarationConfig.scopeContext
        if(scopeContextOnType == null) return scopeContextOnDeclaration
        if(scopeContextOnDeclaration == null) return scopeContextOnType
        val result = scopeContextOnType.resolveNext(scopeContextOnDeclaration)
        result.isExact = false
        return result
    }
}

class ParadoxBaseDefinitionScopeContextProvider: ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return true
    }
    
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val configGroup = definitionInfo.configGroup
        val configs = configGroup.extendedDefinitions.findFromPattern(definitionInfo.name, definition, configGroup).orEmpty()
        val config = configs.findLast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) } ?: return null
        val result = config.config.scopeContext ?: return null
        result.isExact = true
        return result
    }
}

class ParadoxGameRuleScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "game_rule"
    }
    
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedGameRules.findFromPattern(definitionInfo.name, definition, configGroup)
        val result = config?.config?.scopeContext ?: return null
        result.isExact = true
        return result
    }
}

class ParadoxOnActionScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "on_action"
    }
    
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedOnActions.findFromPattern(definitionInfo.name, definition, configGroup)
        val result = config?.config?.scopeContext ?: return null
        result.isExact = true
        return result
    }
}