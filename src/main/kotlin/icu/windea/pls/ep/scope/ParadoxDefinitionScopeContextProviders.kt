package icu.windea.pls.ep.scope

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.findLastFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.scope.isExact

@Optimized
class ParadoxDefaultDefinitionScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return true
    }

    override fun getScopeContext(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val declarationConfig = definitionInfo.declarationConfig?.config ?: return null
        val subtypeConfigs = definitionInfo.subtypeConfigs
        val typeConfig = definitionInfo.typeConfig
        val scopeContextOnType = getScopeContextOnType(typeConfig, subtypeConfigs)
        val scopeContextOnDeclaration = getScopeContextOnDeclaration(declarationConfig)
        if (scopeContextOnType == null) return scopeContextOnDeclaration
        if (scopeContextOnDeclaration == null) return scopeContextOnType
        return scopeContextOnType.resolveNext(scopeContextOnDeclaration).also { it.isExact = false }
    }

    private fun getScopeContextOnType(typeConfig: CwtTypeConfig, subtypeConfigs: List<CwtSubtypeConfig>): ParadoxScopeContext? {
        subtypeConfigs.forEachFast { subtypeConfig ->
            subtypeConfig.config.optionMetadata.scopeContext?.let { return it }
        }
        return typeConfig.config.optionMetadata.scopeContext
    }

    private fun getScopeContextOnDeclaration(declarationConfig: CwtPropertyConfig): ParadoxScopeContext? {
        return declarationConfig.optionMetadata.scopeContext
    }
}

@Optimized
class ParadoxBaseDefinitionScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun supports(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return true
    }

    override fun getScopeContext(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val configGroup = definitionInfo.configGroup
        val configs = configGroup.extendedDefinitions.findByPattern(definitionInfo.name, definition, configGroup).orEmpty()
        val config = configs.findLastFast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) } ?: return null
        return config.config.optionMetadata.scopeContext
    }
}

@Optimized
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

@Optimized
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
