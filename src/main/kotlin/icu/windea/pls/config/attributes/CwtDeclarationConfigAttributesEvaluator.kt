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
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionSubtypeExpression

/**
 * 声明规则的综合属性的评估器。
 *
 * @see CwtDeclarationConfig
 * @see CwtDeclarationConfigAttributes
 */
@Optimized
class CwtDeclarationConfigAttributesEvaluator {
    private val involvedSubtypes = sortedSetOf<String>()
    private var dynamicValueInvolved = false
    private var parameterInvolved = false
    private var localisationParameterInvolved = false
    private var inferredScopeContextAwareDefinitionReferenceInvolved = false

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
        resolved.subtypes.forEachFast { (subtype, _) -> involvedSubtypes.add(subtype) }
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

    private fun buildAttributes(): CwtDeclarationConfigAttributes {
        val result = CwtDeclarationConfigAttributes(
            involvedSubtypes.optimized(),
            dynamicValueInvolved,
            parameterInvolved,
            localisationParameterInvolved,
            inferredScopeContextAwareDefinitionReferenceInvolved,
        )
        if (result == CwtDeclarationConfigAttributes.EMPTY) return CwtDeclarationConfigAttributes.EMPTY
        return result
    }
}
