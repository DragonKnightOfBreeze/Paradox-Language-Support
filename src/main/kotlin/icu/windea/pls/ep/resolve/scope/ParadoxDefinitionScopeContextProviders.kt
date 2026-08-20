package icu.windea.pls.ep.resolve.scope

import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.scope.isExact

class ParadoxDefaultDefinitionScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return true
    }

    override fun getScopeContext(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val declarationConfig = definitionInfo.declarationConfig?.config ?: return null
        val subtypeConfigs = definitionInfo.subtypeConfigs
        val typeConfig = definitionInfo.typeConfig
        val scopeContextOnType = subtypeConfigs.firstNotNullOfOrNull { it.config.optionMetadata.scopeContext }
            ?: typeConfig.config.optionMetadata.scopeContext
        val scopeContextOnDeclaration = declarationConfig.optionMetadata.scopeContext
        if (scopeContextOnType == null) return scopeContextOnDeclaration
        if (scopeContextOnDeclaration == null) return scopeContextOnType
        return scopeContextOnType.resolveNext(scopeContextOnDeclaration).also { it.isExact = false }
    }
}

class ParadoxBaseDefinitionScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return true
    }

    override fun getScopeContext(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val configGroup = definitionInfo.configGroup
        val configs = configGroup.extendedDefinitions.findByPattern(definitionInfo.name, definition, configGroup).orEmpty()
        val config = configs.findLast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) } ?: return null
        return config.config.optionMetadata.scopeContext
    }
}

class ParadoxGameRuleScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == ParadoxDefinitionTypes.gameRule
    }

    override fun getScopeContext(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedGameRules.findByPattern(definitionInfo.name, definition, configGroup) ?: return null
        return config.config.optionMetadata.scopeContext
    }
}

class ParadoxOnActionScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == ParadoxDefinitionTypes.onAction
    }

    override fun getScopeContext(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedOnActions.findByPattern(definitionInfo.name, definition, configGroup) ?: return null
        return config.config.optionMetadata.scopeContext
    }
}
