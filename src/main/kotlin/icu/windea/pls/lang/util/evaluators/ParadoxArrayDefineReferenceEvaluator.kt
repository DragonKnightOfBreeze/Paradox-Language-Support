package icu.windea.pls.lang.util.evaluators

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.resolve.complexExpression.ParadoxArrayDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxLinkedExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.script.psi.ParadoxScriptValue

/**
 * 数组定值引用表达式的评估器。
 *
 * @see ParadoxArrayDefineReferenceExpression
 */
class ParadoxArrayDefineReferenceEvaluator(
    var resolve: Boolean = true,
) {
    fun evaluateFromRoot(element: ParadoxExpressionElement): ParadoxScriptValue? {
        val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return null
        if (config.configExpression.type !in CwtDataTypeSets.ArrayDefineReferenceEvaluatable) return null
        val value = element.value
        val configGroup = config.configGroup
        val rootExpression = ParadoxComplexExpression.resolveByConfig(value, null, configGroup, config) ?: return null
        return evaluateFromRoot(element, rootExpression)
    }

    fun evaluateFromRoot(element: ParadoxExpressionElement, rootExpression: ParadoxComplexExpression): ParadoxScriptValue? {
        // ignore prev link nodes
        val expression = when {
            rootExpression is ParadoxArrayDefineReferenceExpression -> rootExpression
            rootExpression is ParadoxLinkedExpression -> {
                val lastLinkNode = rootExpression.nodes.findLast { it is ParadoxLinkNode } ?: return null
                val lastLinkValueNode = lastLinkNode.nodes.findLast { it is ParadoxLinkValueNode } ?: return null
                val resultNode = lastLinkValueNode.nodes.singleOrNull { it is ParadoxArrayDefineReferenceExpression } ?: return null
                resultNode
            }
            else -> null
        }
        if (expression !is ParadoxArrayDefineReferenceExpression) return null
        return evaluate(element, expression)
    }

    fun evaluate(element: ParadoxExpressionElement, expression: ParadoxArrayDefineReferenceExpression): ParadoxScriptValue? {
        val variableNode = expression.variableNode ?: return null
        val resolved = variableNode.getReference(element)?.resolve() ?: return null
        if (!ParadoxDefineManager.isArrayDefine(resolved)) return null // check if it's an array define
        val indexNode = expression.indexNode ?: return null
        val index = indexNode.text.toIntOrNull()?.takeIf { it >= 0 } ?: return null
        val value = ParadoxDefineManager.getArrayValue(resolved, index) ?: return null
        return if (resolve) value.resolved() else value
    }
}
