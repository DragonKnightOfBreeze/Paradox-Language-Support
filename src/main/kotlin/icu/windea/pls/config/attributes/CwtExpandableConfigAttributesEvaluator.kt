package icu.windea.pls.config.attributes

import icu.windea.pls.config.config.CwtExpandableConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.match.CwtConfigExpressionMatchService
import icu.windea.pls.config.util.CwtConfigVisitorManager
import icu.windea.pls.config.util.CwtMemberConfigExpandedRecursiveVisitor
import icu.windea.pls.core.annotations.Optimized

/**
 * 可展开的规则（并集规则、别名规则、单别名规则）的综合属性的评估器。
 *
 * @see CwtExpandableConfig
 * @see CwtExpandableConfigAttributes
 */
@Optimized
class CwtExpandableConfigAttributesEvaluator {
    private var involveDynamicValue = false
    private var involveParameter = false
    private var involveLocalisationParameter = false
    private var involveInferredScopeContextAwareDefinitionReference = false
    private var involveExternalReference = false

    fun evaluate(name: String, unionConfig: CwtUnionConfig, configGroup: CwtConfigGroup): CwtExpandableConfigAttributes {
        val visitor = buildVisitor(configGroup)
        CwtConfigVisitorManager.visitUnion(name, unionConfig, visitor)
        return buildAttributes()
    }

    fun evaluate(name: String, aliasConfigGroup: Collection<List<CwtAliasConfig>>, configGroup: CwtConfigGroup): CwtExpandableConfigAttributes {
        val visitor = buildVisitor(configGroup)
        CwtConfigVisitorManager.visitAliasGroup(name, aliasConfigGroup, visitor)
        return buildAttributes()
    }

    fun evaluate(name: String, singleAliasConfig: CwtSingleAliasConfig, configGroup: CwtConfigGroup): CwtExpandableConfigAttributes {
        val visitor = buildVisitor(configGroup)
        CwtConfigVisitorManager.visitSingleAlias(name, singleAliasConfig, visitor)
        return buildAttributes()
    }

    private fun buildVisitor(configGroup: CwtConfigGroup): CwtMemberConfigExpandedRecursiveVisitor {
        return object : CwtMemberConfigExpandedRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                processDataExpression(config.keyExpression, configGroup)
                processDataExpression(config.valueExpression, configGroup)
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                processDataExpression(config.valueExpression, configGroup)
                return super.visitValue(config)
            }

            override fun visitUnion(name: String, config: CwtUnionConfig): Boolean {
                val attributes = configGroup.unionAttributes.getOrPut(name) { evaluate(name, config, configGroup) }
                return handleContext(attributes)
            }

            override fun visitAliasGroup(name: String, aliasConfigGroup: Collection<List<CwtAliasConfig>>): Boolean {
                val attributes = configGroup.aliasAttributes.getOrPut(name) { evaluate(name, aliasConfigGroup, configGroup) }
                return handleContext(attributes)
            }

            override fun visitSingleAlias(name: String, config: CwtSingleAliasConfig): Boolean {
                val attributes = configGroup.singleAliasAttributes.getOrPut(name) { evaluate(name, config, configGroup) }
                return handleContext(attributes)
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

    private fun handleContext(attributes: CwtExpandableConfigAttributes): Boolean {
        if (attributes.involveDynamicValue) involveDynamicValue = true
        if (attributes.involveParameter) involveParameter = true
        if (attributes.involveLocalisationParameter) involveLocalisationParameter = true
        if (attributes.involveInferredScopeContextAwareDefinitionReference) involveInferredScopeContextAwareDefinitionReference = true
        if (attributes.involveExternalReference) involveExternalReference = true
        return true
    }

    private fun buildAttributes(): CwtExpandableConfigAttributes {
        val result = CwtExpandableConfigAttributes(
            involveDynamicValue,
            involveParameter,
            involveLocalisationParameter,
            involveInferredScopeContextAwareDefinitionReference,
            involveExternalReference,
        )
        if (result == CwtExpandableConfigAttributes.EMPTY) return CwtExpandableConfigAttributes.EMPTY
        return result
    }
}

