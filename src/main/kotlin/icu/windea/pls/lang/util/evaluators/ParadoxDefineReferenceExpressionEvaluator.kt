package icu.windea.pls.lang.util.evaluators

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxLinkedExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.script.psi.ParadoxScriptValue

/**
 * 定值引用表达式的评估器。
 *
 * @see ParadoxDefineReferenceExpression
 */
class ParadoxDefineReferenceExpressionEvaluator(
    var resolve: Boolean = true,
) {
    fun evaluate(element: ParadoxExpressionElement): ParadoxScriptValue? {
        val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return null
        if (config.configExpression.type !in CwtDataTypeSets.DefineReferenceEvaluatable) return null
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

    fun findExpression(rootExpression: ParadoxComplexExpression): ParadoxDefineReferenceExpression? {
        // NOTE 2.1.10 ignore prev link nodes
        return when (rootExpression) {
            is ParadoxDefineReferenceExpression -> rootExpression
            is ParadoxLinkedExpression -> {
                val lastLinkNode = rootExpression.nodes.findLast { it is ParadoxLinkNode } ?: return null
                val lastLinkValueNode = lastLinkNode.nodes.findLast { it is ParadoxLinkValueNode } ?: return null
                val resultNode = lastLinkValueNode.nodes.singleOrNull() ?: return null
                resultNode.castOrNull()
            }
            else -> null
        }
    }

    fun evaluateExpression(element: ParadoxExpressionElement, expression: ParadoxDefineReferenceExpression): ParadoxScriptValue? {
        val variableNode = expression.variableNode ?: return null
        val resolved = variableNode.getReference(element)?.resolve() ?: return null
        val value = resolved.propertyValue ?: return null
        return if (resolve) value.resolved() else value
    }
}
