package icu.windea.pls.lang.resolve.complexExpression.util

import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxLinkedExpression
import icu.windea.pls.lang.resolve.complexExpression.linkNodes
import icu.windea.pls.lang.resolve.complexExpression.nodes.*

interface ParadoxComplexExpressionValidatorScope {
     fun validateAllNodes(expression: ParadoxComplexExpression, errors: MutableList<ParadoxComplexExpressionError>, processor: Processor<ParadoxComplexExpressionNode>): Boolean {
        var result = true
        expression.accept(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (node is ParadoxComplexExpression && node !== expression) {
                    errors += node.getErrors()
                    return true
                }
                if (node is ParadoxErrorTokenNode || node.text.isEmpty()) {
                    result = false
                }
                if (result) result = processor.process(node)
                return super.visit(node)
            }
        })
        return result
    }

     fun processLinkedExpression(expression: ParadoxLinkedExpression, element: ParadoxExpressionElement?, errors: MutableList<ParadoxComplexExpressionError>) {
        // 如果链式表达式使用包含前缀的嵌套的链式表达式和前缀作为其传参，则需要用双引号括起
        checkQuotes(element, expression, errors)
    }

     fun checkQuotes(element: ParadoxExpressionElement?, expression: ParadoxLinkedExpression, errors: MutableList<ParadoxComplexExpressionError>) {
        if (expression is ParadoxCommandExpression) return // 2.1.1 目前直接跳过
        if (element == null || !element.text.let { !it.isLeftQuoted() || !it.isRightQuoted() }) return
        val r = expression.linkNodes.process p@{ linkNode ->
            val prefixNode = linkNode.nodes.getOrNull(0)?.takeIf { it is ParadoxLinkPrefixNode }
            if (prefixNode == null) return@p true
            val leftParNode = linkNode.nodes.getOrNull(1)?.takeIf { it is ParadoxMarkerNode && it.text == "(" }
            if (leftParNode == null) return@p true
            val valueNode = linkNode.nodes.getOrNull(2)?.takeIf { it is ParadoxLinkValueNode }
            if (valueNode == null) return@p true
            ":" !in valueNode.text
        }
        if (!r) errors += ParadoxComplexExpressionErrors.notQuotedNestedWithPrefixes(expression.rangeInExpression)
    }
}
