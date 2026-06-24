package icu.windea.pls.lang.resolve.complexExpression.util

import com.intellij.util.Processor
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

interface ParadoxComplexExpressionValidatorScope {
    fun validateAllNodes(expression: ParadoxComplexExpression, element: ParadoxExpressionElement? = null, errors: MutableList<ParadoxComplexExpressionError>, processor: Processor<ParadoxComplexExpressionNode>): Boolean {
        var result = true
        expression.accept(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (node is ParadoxComplexExpression && node !== expression) {
                    errors += node.getErrors(element)
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

    fun checkQuotes(element: ParadoxExpressionElement?, expression: ParadoxComplexExpression, errors: MutableList<ParadoxComplexExpressionError>) {
        // 脚本文件中的复杂表达式如果包含英文括号或英文逗号，应用双引号括起
        if (element !is ParadoxScriptExpressionElement) return // 仅适用于脚本文件中的
        if (!element.text.let { !it.isLeftQuoted() || !it.isRightQuoted() }) return
        val containSpecialChar = expression.text.any { it == '(' || it == ')' || it == ',' }
        if (containSpecialChar) errors += ParadoxComplexExpressionErrors.notQuoted(expression.rangeInExpression)
    }
}
