package icu.windea.pls.config.attributes

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.match.CwtConfigExpressionMatchService
import icu.windea.pls.config.util.CwtMemberConfigInlinedRecursiveVisitor
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.optimized
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.model.expressions.ParadoxDefinitionSubtypeExpression

/**
 * 声明规则的综合属性的评估器。
 *
 * @see CwtDeclarationConfig
 * @see CwtDeclarationConfigAttributes
 */
@Optimized
class CwtDeclarationConfigAttributesEvaluator {
    private val involvedSubtypes = sortedSetOf<String>()
    private var involveDynamicValue = false
    private var involveParameter = false
    private var involveLocalisationParameter = false
    private var involveInferredScopeContextAwareDefinitionReference = false
    private var involveExternalReference = false

    fun evaluate(config: CwtDeclarationConfig): CwtDeclarationConfigAttributes {
        val visitor = buildVisitor(config.configGroup)
        config.config.accept(visitor)
        return buildAttributes()
    }

    private fun buildVisitor(configGroup: CwtConfigGroup): CwtMemberConfigInlinedRecursiveVisitor {
        return object : CwtMemberConfigInlinedRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                if (!inlined) processSubtypeExpression(config)
                processDataExpression(config.keyExpression, configGroup)
                processDataExpression(config.valueExpression, configGroup)
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                processDataExpression(config.valueExpression, configGroup)
                return super.visitValue(config)
            }

            override fun visitAliasGroup(name: String, aliasConfigGroup: Collection<List<CwtAliasConfig>>): Boolean {
                val inlinedAttributes = configGroup.aliasAttributes.getOrPut(name) {
                    CwtInlinedConfigAttributesEvaluator().evaluate(name, aliasConfigGroup, configGroup)
                }
                return handleContext(inlinedAttributes)
            }

            override fun visitSingleAlias(name: String, config: CwtSingleAliasConfig): Boolean {
                val inlinedAttributes = configGroup.singleAliasAttributes.getOrPut(name) {
                    CwtInlinedConfigAttributesEvaluator().evaluate(name, config, configGroup)
                }
                return handleContext(inlinedAttributes)
            }
        }
    }

    private fun processSubtypeExpression(config: CwtPropertyConfig) {
        val subtypeExpression = config.key.removeSurroundingOrNull("subtype[", "]") ?: return
        val resolved = ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression)
        resolved.parts.forEachFast { (subtype, _) -> involvedSubtypes.add(subtype) }
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

    private fun buildAttributes(): CwtDeclarationConfigAttributes {
        val result = CwtDeclarationConfigAttributes(
            involvedSubtypes.optimized(),
            involveDynamicValue,
            involveParameter,
            involveLocalisationParameter,
            involveInferredScopeContextAwareDefinitionReference,
            involveExternalReference,
        )
        if (result == CwtDeclarationConfigAttributes.EMPTY) return CwtDeclarationConfigAttributes.EMPTY
        return result
    }
}
