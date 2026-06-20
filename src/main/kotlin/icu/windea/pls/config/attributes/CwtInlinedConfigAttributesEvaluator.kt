package icu.windea.pls.config.attributes

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.match.CwtConfigExpressionMatchService
import icu.windea.pls.config.util.CwtConfigVisitorManager
import icu.windea.pls.config.util.CwtMemberConfigInlinedRecursiveVisitor
import icu.windea.pls.core.annotations.Optimized

/**
 * 要内联的规则（单别名规则、别名规则）的综合属性的评估器。
 *
 * @see CwtSingleAliasConfig
 * @see CwtAliasConfig
 * @see CwtInlinedConfigAttributes
 */
@Optimized
class CwtInlinedConfigAttributesEvaluator {
    private var involvesDynamicValue = false
    private var involvesParameter = false
    private var involvesLocalisationParameter = false
    private var involvesInferredScopeContextAwareDefinitionReference = false
    private var involvesExternalReference = false

    fun evaluate(name: String, singleAliasConfig: CwtSingleAliasConfig, configGroup: CwtConfigGroup): CwtInlinedConfigAttributes {
        val visitor = buildVisitor(configGroup)
        CwtConfigVisitorManager.visitSingleAlias(name, singleAliasConfig, visitor)
        return buildAttributes()
    }

    fun evaluate(name: String, aliasConfigGroup: Collection<List<CwtAliasConfig>>, configGroup: CwtConfigGroup): CwtInlinedConfigAttributes {
        val visitor = buildVisitor(configGroup)
        CwtConfigVisitorManager.visitAliasGroup(name, aliasConfigGroup, visitor)
        return buildAttributes()
    }

    private fun buildVisitor(configGroup: CwtConfigGroup): CwtMemberConfigInlinedRecursiveVisitor {
        return object : CwtMemberConfigInlinedRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                processDataExpression(config.keyExpression, configGroup)
                processDataExpression(config.valueExpression, configGroup)
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                processDataExpression(config.valueExpression, configGroup)
                return super.visitValue(config)
            }

            override fun visitSingleAlias(name: String, config: CwtSingleAliasConfig): Boolean {
                val inlinedAttributes = configGroup.singleAliasAttributes.getOrPut(name) { evaluate(name, config, configGroup) }
                return handleContext(inlinedAttributes)
            }

            override fun visitAliasGroup(name: String, aliasConfigGroup: Collection<List<CwtAliasConfig>>): Boolean {
                val inlinedAttributes = configGroup.aliasAttributes.getOrPut(name) { evaluate(name, aliasConfigGroup, configGroup) }
                return handleContext(inlinedAttributes)
            }
        }
    }

    private fun processDataExpression(dataExpression: CwtDataExpression, configGroup: CwtConfigGroup) {
        if (!involvesDynamicValue) {
            val r = CwtConfigExpressionMatchService.matchesDynamicValue(dataExpression)
            if (r) involvesDynamicValue = true
        }
        if (!involvesParameter) {
            val r = CwtConfigExpressionMatchService.matchesParameter(dataExpression)
            if (r) involvesParameter = true
        }
        if (!involvesLocalisationParameter) {
            val r = CwtConfigExpressionMatchService.matchesLocalisationParameter(dataExpression)
            if (r) involvesLocalisationParameter = true
        }
        if (!involvesInferredScopeContextAwareDefinitionReference) {
            val r = CwtConfigExpressionMatchService.matchesInferredScopeContextAwareDefinitionReference(dataExpression, configGroup)
            if (r) involvesInferredScopeContextAwareDefinitionReference = true
        }
        if (!involvesExternalReference) {
            val r = CwtConfigExpressionMatchService.matchesExternalReference(dataExpression)
            if (r) involvesExternalReference = true
        }
    }

    private fun handleContext(attributes: CwtInlinedConfigAttributes): Boolean {
        if (attributes.involvesDynamicValue) involvesDynamicValue = true
        if (attributes.involvesParameter) involvesParameter = true
        if (attributes.involvesLocalisationParameter) involvesLocalisationParameter = true
        if (attributes.involvesInferredScopeContextAwareDefinitionReference) involvesInferredScopeContextAwareDefinitionReference = true
        if (attributes.involvesExternalReference) involvesExternalReference = true
        return true
    }

    private fun buildAttributes(): CwtInlinedConfigAttributes {
        val result = CwtInlinedConfigAttributes(
            involvesDynamicValue,
            involvesParameter,
            involvesLocalisationParameter,
            involvesInferredScopeContextAwareDefinitionReference,
            involvesExternalReference,
        )
        if (result == CwtInlinedConfigAttributes.EMPTY) return CwtInlinedConfigAttributes.EMPTY
        return result
    }
}

