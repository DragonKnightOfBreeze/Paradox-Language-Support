package icu.windea.pls.ep.scope

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

class ParadoxDefaultDefinitionScopeContextProvider : ParadoxDefinitionScopeContextProvider {
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
        if (scopeContextOnType == null) return scopeContextOnDeclaration
        if (scopeContextOnDeclaration == null) return scopeContextOnType
        return scopeContextOnType.resolveNext(scopeContextOnDeclaration).also { it.isExact = false }
    }
}

class ParadoxBaseDefinitionScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return true
    }

    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val configGroup = definitionInfo.configGroup
        val configs = configGroup.extendedDefinitions.findFromPattern(definitionInfo.name, definition, configGroup).orEmpty()
        val config = configs.findLast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) } ?: return null
        return config.config.scopeContext
    }
}

class ParadoxGameRuleScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == ParadoxDefinitionTypes.GameRule
    }

    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedGameRules.findFromPattern(definitionInfo.name, definition, configGroup)
        return config?.config?.scopeContext
    }
}

class ParadoxOnActionScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == ParadoxDefinitionTypes.OnAction
    }

    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedOnActions.findFromPattern(definitionInfo.name, definition, configGroup)
        return config?.config?.scopeContext
    }
}
