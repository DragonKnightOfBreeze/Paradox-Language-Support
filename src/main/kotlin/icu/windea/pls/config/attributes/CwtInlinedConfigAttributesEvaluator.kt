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
    private var involveDynamicValue = false
    private var involveParameter = false
    private var involveLocalisationParameter = false
    private var involveInferredScopeContextAwareDefinitionReference = false
    private var involveExternalReference = false

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
        if (!involveDynamicValue) {
            val r = CwtConfigExpressionMatchService.matchesDynamicValue(dataExpression)
            if (r) involveDynamicValue = true
        }
        if (!involveParameter) {
            val r = CwtConfigExpressionMatchService.matchesParameter(dataExpression)
            if (r) involveParameter = true
        }
        if (!involveLocalisationParameter) {
            val r = CwtConfigExpressionMatchService.matchesLocalisationParameter(dataExpression)
            if (r) involveLocalisationParameter = true
        }
        if (!involveInferredScopeContextAwareDefinitionReference) {
            val r = CwtConfigExpressionMatchService.matchesInferredScopeContextAwareDefinitionReference(dataExpression, configGroup)
            if (r) involveInferredScopeContextAwareDefinitionReference = true
        }
        if (!involveExternalReference) {
            val r = CwtConfigExpressionMatchService.matchesExternalReference(dataExpression)
            if (r) involveExternalReference = true
        }
    }

    private fun handleContext(attributes: CwtInlinedConfigAttributes): Boolean {
        if (attributes.involveDynamicValue) involveDynamicValue = true
        if (attributes.involveParameter) involveParameter = true
        if (attributes.involveLocalisationParameter) involveLocalisationParameter = true
        if (attributes.involveInferredScopeContextAwareDefinitionReference) involveInferredScopeContextAwareDefinitionReference = true
        if (attributes.involveExternalReference) involveExternalReference = true
        return true
    }

    private fun buildAttributes(): CwtInlinedConfigAttributes {
        val result = CwtInlinedConfigAttributes(
            involveDynamicValue,
            involveParameter,
            involveLocalisationParameter,
            involveInferredScopeContextAwareDefinitionReference,
            involveExternalReference,
        )
        if (result == CwtInlinedConfigAttributes.EMPTY) return CwtInlinedConfigAttributes.EMPTY
        return result
    }
}

