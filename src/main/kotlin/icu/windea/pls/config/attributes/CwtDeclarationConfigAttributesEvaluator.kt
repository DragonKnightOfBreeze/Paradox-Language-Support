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
    private var involvesDynamicValue = false
    private var involvesParameter = false
    private var involvesLocalisationParameter = false
    private var involvesInferredScopeContextAwareDefinitionReference = false
    private var involvesExternalReference = false

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

            override fun visitSingleAlias(name: String, config: CwtSingleAliasConfig): Boolean {
                val inlinedAttributes = configGroup.singleAliasAttributes.getOrPut(name) {
                    CwtInlinedConfigAttributesEvaluator().evaluate(name, config, configGroup)
                }
                return handleContext(inlinedAttributes)
            }

            override fun visitAliasGroup(name: String, aliasConfigGroup: Collection<List<CwtAliasConfig>>): Boolean {
                val inlinedAttributes = configGroup.aliasAttributes.getOrPut(name) {
                    CwtInlinedConfigAttributesEvaluator().evaluate(name, aliasConfigGroup, configGroup)
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
            val r = CwtConfigExpressionMatchService.matchesMeshLocator(dataExpression)
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

    private fun buildAttributes(): CwtDeclarationConfigAttributes {
        val result = CwtDeclarationConfigAttributes(
            involvedSubtypes.optimized(),
            involvesDynamicValue,
            involvesParameter,
            involvesLocalisationParameter,
            involvesInferredScopeContextAwareDefinitionReference,
            involvesExternalReference,
        )
        if (result == CwtDeclarationConfigAttributes.EMPTY) return CwtDeclarationConfigAttributes.EMPTY
        return result
    }
}
