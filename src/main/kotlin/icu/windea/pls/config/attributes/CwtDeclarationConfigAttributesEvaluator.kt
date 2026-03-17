package icu.windea.pls.config.attributes

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtMemberConfigInlinedRecursiveVisitor
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.optimized
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionSubtypeExpression

/**
 * 声明规则的综合属性的评估器。
 *
 * @see CwtDeclarationConfig
 * @see CwtDeclarationConfigAttributes
 */
@Optimized
object CwtDeclarationConfigAttributesEvaluator {
    private data class Context(
        val involvedSubtypes: MutableSet<String> = sortedSetOf(),
        var dynamicValueInvolved: Boolean = false,
        var parameterInvolved: Boolean = false,
        var localisationParameterInvolved: Boolean = false,
        var inferredScopeContextAwareDefinitionReferenceInvolved: Boolean = false,
    )

    fun evaluate(config: CwtDeclarationConfig): CwtDeclarationConfigAttributes {
        val context = Context()
        val visitor = buildVisitor(context, config.configGroup)
        config.config.accept(visitor)
        return buildAttributes(context)
    }

    private fun buildVisitor(context: Context, configGroup: CwtConfigGroup): CwtMemberConfigInlinedRecursiveVisitor {
        return object : CwtMemberConfigInlinedRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                if (!inlined) processSubtypeExpression(context, config)
                processDataExpression(context, config.keyExpression, configGroup)
                processDataExpression(context, config.valueExpression, configGroup)
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                processDataExpression(context, config.valueExpression, configGroup)
                return super.visitValue(config)
            }

            override fun visitSingleAlias(name: String, config: CwtSingleAliasConfig): Boolean {
                val inlinedAttributes = configGroup.singleAliasAttributes.getOrPut(name) { CwtInlinedConfigAttributesEvaluator.evaluate(name, config, configGroup) }
                return handleContext(context, inlinedAttributes)
            }

            override fun visitAliasGroup(name: String, aliasConfigGroup: Collection<List<CwtAliasConfig>>): Boolean {
                val inlinedAttributes = configGroup.aliasAttributes.getOrPut(name) { CwtInlinedConfigAttributesEvaluator.evaluate(name, aliasConfigGroup, configGroup) }
                return handleContext(context, inlinedAttributes)
            }
        }
    }

    private fun processSubtypeExpression(context: Context, config: CwtPropertyConfig) {
        val subtypeExpression = config.key.removeSurroundingOrNull("subtype[", "]") ?: return
        val resolved = ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression)
        resolved.subtypes.forEachFast { (subtype, _) -> context.involvedSubtypes.add(subtype) }
    }

    private fun processDataExpression(context: Context, dataExpression: CwtDataExpression, configGroup: CwtConfigGroup) {
        if (!context.dynamicValueInvolved) {
            val r = CwtConfigAttributesUtil.dynamicValueInvolved(dataExpression)
            if (r) context.dynamicValueInvolved = true
        }
        if (!context.parameterInvolved) {
            val r = CwtConfigAttributesUtil.parameterInvolved(dataExpression)
            if (r) context.parameterInvolved = true
        }
        if (!context.localisationParameterInvolved) {
            val r = CwtConfigAttributesUtil.localisationParameterInvolved(dataExpression)
            if (r) context.localisationParameterInvolved = true
        }
        if (!context.inferredScopeContextAwareDefinitionReferenceInvolved) {
            val r = CwtConfigAttributesUtil.inferredScopeContextAwareDefinitionReferenceInvolved(dataExpression, configGroup)
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

    private fun buildAttributes(context: Context): CwtDeclarationConfigAttributes {
        val result = CwtDeclarationConfigAttributes(
            context.involvedSubtypes.optimized(),
            context.dynamicValueInvolved,
            context.parameterInvolved,
            context.localisationParameterInvolved,
            context.inferredScopeContextAwareDefinitionReferenceInvolved,
        )
        if (result == CwtDeclarationConfigAttributes.EMPTY) return CwtDeclarationConfigAttributes.EMPTY
        return result
    }
}
