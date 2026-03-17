package icu.windea.pls.config.attributes

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.configExpression.CwtConfigExpressionMatcher
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtMemberConfigInlinedRecursiveVisitor
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.annotations.Optimized

/**
 * 要内联的规则（单别名规则、别名规则）的综合属性的评估器。
 *
 * @see CwtSingleAliasConfig
 * @see CwtAliasConfig
 * @see CwtInlinedConfigAttributes
 */
@Optimized
object CwtInlinedConfigAttributesEvaluator {
    private data class Context(
        val involvedSubtypes: MutableSet<String> = sortedSetOf(),
        var dynamicValueInvolved: Boolean = false,
        var parameterInvolved: Boolean = false,
        var localisationParameterInvolved: Boolean = false,
        var inferredScopeContextAwareDefinitionReferenceInvolved: Boolean = false,
    )

    fun evaluate(name: String, singleAliasConfig: CwtSingleAliasConfig, configGroup: CwtConfigGroup): CwtInlinedConfigAttributes {
        val context = Context()
        val visitor = buildVisitor(context, configGroup)
        CwtConfigManipulator.visitSingleAlias(name, singleAliasConfig, visitor)
        return buildAttributes(context)
    }

    fun evaluate(name: String, aliasConfigGroup: Collection<List<CwtAliasConfig>>, configGroup: CwtConfigGroup): CwtInlinedConfigAttributes {
        val context = Context()
        val visitor = buildVisitor(context, configGroup)
        CwtConfigManipulator.visitAliasGroup(name, aliasConfigGroup, visitor)
        return buildAttributes(context)
    }

    private fun buildVisitor(context: Context, configGroup: CwtConfigGroup): CwtMemberConfigInlinedRecursiveVisitor {
        return object : CwtMemberConfigInlinedRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                processDataExpression(context, config.keyExpression, configGroup)
                processDataExpression(context, config.valueExpression, configGroup)
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                processDataExpression(context, config.valueExpression, configGroup)
                return super.visitValue(config)
            }

            override fun visitSingleAlias(name: String, config: CwtSingleAliasConfig): Boolean {
                val inlinedAttributes = configGroup.singleAliasAttributes.getOrPut(name) { evaluate(name, config, configGroup) }
                return handleContext(context, inlinedAttributes)
            }

            override fun visitAliasGroup(name: String, aliasConfigGroup: Collection<List<CwtAliasConfig>>): Boolean {
                val inlinedAttributes = configGroup.aliasAttributes.getOrPut(name) { evaluate(name, aliasConfigGroup, configGroup) }
                return handleContext(context, inlinedAttributes)
            }
        }
    }

    private fun processDataExpression(context: Context, dataExpression: CwtDataExpression, configGroup: CwtConfigGroup) {
        if (!context.dynamicValueInvolved) {
            val r = CwtConfigExpressionMatcher.isDynamicValue(dataExpression)
            if (r) context.dynamicValueInvolved = true
        }
        if (!context.parameterInvolved) {
            val r = CwtConfigExpressionMatcher.isParameter(dataExpression)
            if (r) context.parameterInvolved = true
        }
        if (!context.localisationParameterInvolved) {
            val r = CwtConfigExpressionMatcher.isLocalisationParameter(dataExpression)
            if (r) context.localisationParameterInvolved = true
        }
        if (!context.inferredScopeContextAwareDefinitionReferenceInvolved) {
            val r = CwtConfigExpressionMatcher.isInferredScopeContextAwareDefinitionReference(dataExpression, configGroup)
            if (r) context.inferredScopeContextAwareDefinitionReferenceInvolved = true
        }
    }

    private fun handleContext(context: Context, inlinedAttributes: CwtInlinedConfigAttributes): Boolean {
        if (inlinedAttributes.dynamicValueInvolved) context.dynamicValueInvolved = true
        if (inlinedAttributes.parameterInvolved) context.parameterInvolved = true
        if (inlinedAttributes.localisationParameterInvolved) context.localisationParameterInvolved = true
        if (inlinedAttributes.inferredScopeContextAwareDefinitionReferenceInvolved) context.inferredScopeContextAwareDefinitionReferenceInvolved = true
        return true
    }

    private fun buildAttributes(context: Context): CwtInlinedConfigAttributes {
        val result = CwtInlinedConfigAttributes(
            context.dynamicValueInvolved,
            context.parameterInvolved,
            context.localisationParameterInvolved,
            context.inferredScopeContextAwareDefinitionReferenceInvolved,
        )
        if (result == CwtInlinedConfigAttributes.EMPTY) return CwtInlinedConfigAttributes.EMPTY
        if (result == CwtInlinedConfigAttributes.ALL) return CwtInlinedConfigAttributes.ALL
        return result
    }
}

