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
 * 复杂表达式的评估器。
 *
 * 说明：
 * - 输入的可以是上层的复杂表达式，也可以是最终需要评估的复杂表达式。
 * - 评估结果可能来自其中嵌套的特定复杂表达式。
 *
 * @see ParadoxComplexExpression
 */
class ParadoxComplexExpressionEvaluator(
    var resolve: Boolean = true,
) {
    fun evaluate(element: ParadoxExpressionElement): ParadoxScriptValue? {
        val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return null
        if (config.configExpression.type !in CwtDataTypeSets.Evaluatable) return null
        val rootExpression = ParadoxComplexExpression.resolveByConfig(element.value, null, config.configGroup, config) ?: return null
        val expression = findExpression(rootExpression) ?: return null
        return evaluateExpression(element, expression)
    }

    fun evaluate(element: ParadoxExpressionElement, rootExpression: ParadoxComplexExpression): ParadoxScriptValue? {
        val expression = findExpression(rootExpression) ?: return null
        return evaluateExpression(element, expression)
    }

    fun findExpression(rootExpression: ParadoxComplexExpression): ParadoxComplexExpression? {
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

    fun evaluateExpression(element: ParadoxExpressionElement, expression: ParadoxComplexExpression): ParadoxScriptValue? {
        return when (expression) {
            is ParadoxDefineReferenceExpression -> ParadoxDefineReferenceExpressionEvaluator(resolve).evaluateExpression(element, expression)
            is ParadoxArrayDefineReferenceExpression -> ParadoxArrayDefineReferenceExpressionEvaluator(resolve).evaluateExpression(element, expression)
            else -> null
        }
    }
}
