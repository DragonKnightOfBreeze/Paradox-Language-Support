package icu.windea.pls.lang.resolve.complexExpression.util

import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxLinkedExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTagsExpression
import icu.windea.pls.lang.resolve.complexExpression.linkNodes
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

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

    fun checkQuotesForLinkedExpression(element: ParadoxExpressionElement?, expression: ParadoxLinkedExpression, errors: MutableList<ParadoxComplexExpressionError>) {
        // 如果脚本文件中的链式表达式使用包含前缀的嵌套的链式表达式和前缀作为其传参，则需要用双引号括起
        if (element !is ParadoxScriptExpressionElement) return // 仅适用于脚本文件中的
        if (!element.text.let { !it.isLeftQuoted() || !it.isRightQuoted() }) return
        val r = expression.acceptChildren(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if(node is ParadoxMarkerNode) return false
                return super.visit(node)
            }
        })
        if (!r) errors += ParadoxComplexExpressionErrors.linkExpressionNotQuoted(expression.rangeInExpression)
    }

    fun checkQuotesForTagsExpression(element: ParadoxExpressionElement?, expression: ParadoxTagsExpression, errors: MutableList<ParadoxComplexExpressionError>) {
        // 如果脚本文件中的标签集合表达式包含括号或逗号，则需要用双引号括起
        if(element !is ParadoxScriptExpressionElement) return // 仅适用于脚本文件中的
        if (!element.text.let { !it.isLeftQuoted() || !it.isRightQuoted() }) return
        val r = expression.acceptChildren(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if(node is ParadoxMarkerNode) return false
                return super.visit(node)
            }
        })
        if (!r) errors += ParadoxComplexExpressionErrors.tagsExpressionNotQuoted(expression.rangeInExpression)
    }
}
