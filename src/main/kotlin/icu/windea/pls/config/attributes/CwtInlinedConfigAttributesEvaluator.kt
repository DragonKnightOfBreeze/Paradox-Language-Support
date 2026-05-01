package icu.windea.pls.config.attributes

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.manipulators.CwtConfigManipulator
import icu.windea.pls.config.match.CwtConfigExpressionMatchService
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
    private var dynamicValueInvolved = false
    private var parameterInvolved = false
    private var localisationParameterInvolved = false
    private var inferredScopeContextAwareDefinitionReferenceInvolved = false

    fun evaluate(name: String, singleAliasConfig: CwtSingleAliasConfig, configGroup: CwtConfigGroup): CwtInlinedConfigAttributes {
        val visitor = buildVisitor(configGroup)
        CwtConfigManipulator.visitSingleAlias(name, singleAliasConfig, visitor)
        return buildAttributes()
    }

    fun evaluate(name: String, aliasConfigGroup: Collection<List<CwtAliasConfig>>, configGroup: CwtConfigGroup): CwtInlinedConfigAttributes {
        val visitor = buildVisitor(configGroup)
        CwtConfigManipulator.visitAliasGroup(name, aliasConfigGroup, visitor)
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
        if (!dynamicValueInvolved) {
            val r = CwtConfigExpressionMatchService.matchesDynamicValue(dataExpression)
            if (r) dynamicValueInvolved = true
        }
        if (!parameterInvolved) {
            val r = CwtConfigExpressionMatchService.matchesParameter(dataExpression)
            if (r) parameterInvolved = true
        }
        if (!localisationParameterInvolved) {
            val r = CwtConfigExpressionMatchService.matchesLocalisationParameter(dataExpression)
            if (r) localisationParameterInvolved = true
        }
        if (!inferredScopeContextAwareDefinitionReferenceInvolved) {
            val r = CwtConfigExpressionMatchService.matchesInferredScopeContextAwareDefinitionReference(dataExpression, configGroup)
            if (r) inferredScopeContextAwareDefinitionReferenceInvolved = true
        }
    }

    private fun handleContext(attributes: CwtInlinedConfigAttributes): Boolean {
        if (attributes.dynamicValueInvolved) dynamicValueInvolved = true
        if (attributes.parameterInvolved) parameterInvolved = true
        if (attributes.localisationParameterInvolved) localisationParameterInvolved = true
        if (attributes.inferredScopeContextAwareDefinitionReferenceInvolved) inferredScopeContextAwareDefinitionReferenceInvolved = true
        return true
    }

    private fun buildAttributes(): CwtInlinedConfigAttributes {
        val result = CwtInlinedConfigAttributes(
            dynamicValueInvolved,
            parameterInvolved,
            localisationParameterInvolved,
            inferredScopeContextAwareDefinitionReferenceInvolved,
        )
        if (result == CwtInlinedConfigAttributes.EMPTY) return CwtInlinedConfigAttributes.EMPTY
        if (result == CwtInlinedConfigAttributes.ALL) return CwtInlinedConfigAttributes.ALL
        return result
    }
}

