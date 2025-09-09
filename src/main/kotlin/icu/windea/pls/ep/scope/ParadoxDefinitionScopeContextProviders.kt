package icu.windea.pls.ep.scope

import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.configGroup.extendedDefinitions
import icu.windea.pls.config.configGroup.extendedGameRules
import icu.windea.pls.config.configGroup.extendedOnActions
import icu.windea.pls.config.findFromPattern
import icu.windea.pls.lang.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxScopeContext
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.isExact
import icu.windea.pls.model.resolveNext
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class ParadoxDefaultDefinitionScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return true
    }

    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val declarationConfig = definitionInfo.declarationConfig?.config ?: return null
        val subtypeConfigs = definitionInfo.subtypeConfigs
        val typeConfig = definitionInfo.typeConfig
        val scopeContextOnType = subtypeConfigs.firstNotNullOfOrNull { it.config.optionData { scopeContext } }
            ?: typeConfig.config.optionData { scopeContext }
        val scopeContextOnDeclaration = declarationConfig.optionData { scopeContext }
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
        return config.config.optionData { scopeContext }
    }
}

class ParadoxGameRuleScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == ParadoxDefinitionTypes.GameRule
    }

    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedGameRules.findFromPattern(definitionInfo.name, definition, configGroup)
        return config?.config?.optionData { scopeContext }
    }
}

class ParadoxOnActionScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == ParadoxDefinitionTypes.OnAction
    }

    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedOnActions.findFromPattern(definitionInfo.name, definition, configGroup)
        return config?.config?.optionData { scopeContext }
    }
}
