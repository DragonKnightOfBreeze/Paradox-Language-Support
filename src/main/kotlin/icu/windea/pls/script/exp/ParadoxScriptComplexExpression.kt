package icu.windea.pls.script.exp

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.core.codeInsight.completion.*

interface ParadoxScriptComplexExpression : ParadoxScriptExpression, ParadoxScriptExpressionNode

fun ParadoxScriptComplexExpression.applyCompletion(context: ProcessingContext, result: CompletionResultSet) {
	if(nodes.isNotEmpty()) {
		val offsetInParent = context.offsetInParent
		for(node in nodes) {
			if(offsetInParent in node.rangeInExpression) {
				node.doApplyCompletion(context, result)
			}
		}
	}
}

private fun ParadoxScriptExpressionNode.doApplyCompletion(context: ProcessingContext, result: CompletionResultSet) {
	this.complete(context, result)
	if(nodes.isNotEmpty()) {
		val offsetInParent = context.offsetInParent
		for(node in nodes) {
			if(offsetInParent in node.rangeInExpression) {
				node.doApplyCompletion(context, result)
			}
		}
	}
}
