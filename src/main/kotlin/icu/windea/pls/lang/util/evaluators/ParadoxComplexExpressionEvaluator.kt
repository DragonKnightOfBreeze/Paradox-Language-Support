package icu.windea.pls.lang.util.evaluators

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxArrayDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxLinkedExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.script.psi.ParadoxScriptValue

/**
 * 各种复杂表达式的评估器。
 *
 * @see ParadoxDefineReferenceExpressionEvaluator
 * @see ParadoxArrayDefineReferenceExpressionEvaluator
 */
@Suppress("unused")
class ParadoxComplexExpressionEvaluator(
    var resolve: Boolean = true,
) {
    fun evaluate(element: ParadoxExpressionElement): ParadoxScriptValue? {
        val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return null
        if (config.configExpression.type !in CwtDataTypeSets.EvaluatableComplexExpression) return null
        val value = element.value
        val configGroup = config.configGroup
        val rootExpression = ParadoxComplexExpression.resolveByConfig(value, null, configGroup, config) ?: return null
        val expression = findExpression(rootExpression) ?: return null
        return evaluateExpression(element, expression)
    }

    fun evaluate(element: ParadoxExpressionElement, rootExpression: ParadoxComplexExpression): ParadoxScriptValue? {
        val expression = findExpression(rootExpression) ?: return null
        return evaluateExpression(element, expression)
    }

    private fun findExpression(rootExpression: ParadoxComplexExpression): ParadoxComplexExpression? {
        // NOTE 2.1.10 ignore prev link nodes
        return when (rootExpression) {
            is ParadoxDefineReferenceExpression -> rootExpression
            is ParadoxArrayDefineReferenceExpression -> rootExpression
            is ParadoxLinkedExpression -> {
                val lastLinkNode = rootExpression.nodes.findLast { it is ParadoxLinkNode } ?: return null
                val lastLinkValueNode = lastLinkNode.nodes.findLast { it is ParadoxLinkValueNode } ?: return null
                val resultNode = lastLinkValueNode.nodes.singleOrNull() ?: return null
                resultNode.castOrNull()
            }
            else -> null
        }
    }

    private fun evaluateExpression(element: ParadoxExpressionElement, expression: ParadoxComplexExpression): ParadoxScriptValue? {
        return when (expression) {
            is ParadoxDefineReferenceExpression -> ParadoxDefineReferenceExpressionEvaluator(resolve).evaluateExpression(element, expression)
            is ParadoxArrayDefineReferenceExpression -> ParadoxArrayDefineReferenceExpressionEvaluator(resolve).evaluateExpression(element, expression)
            else -> null
        }
    }
}
