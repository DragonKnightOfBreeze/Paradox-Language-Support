package icu.windea.pls.script.exp

import com.intellij.codeInsight.completion.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.exp.nodes.*
import icu.windea.pls.script.psi.*

interface ParadoxScriptComplexExpression : ParadoxScriptExpression, ParadoxScriptExpressionNode {
	fun complete(context: ProcessingContext, result: CompletionResultSet) = pass()
}

fun ParadoxScriptComplexExpression.processAllNodes(processor: Processor<ParadoxScriptExpressionNode>): Boolean {
	return doProcessAllNodes(processor)
}

private fun ParadoxScriptExpressionNode.doProcessAllNodes(processor: Processor<ParadoxScriptExpressionNode>): Boolean {
	val r = processor.process(this)
	if(!r) return false
	if(nodes.isNotEmpty()) {
		for(node in nodes) {
			val r1 = node.doProcessAllNodes(processor)
			if(!r1) return false
		}
	}
	return true
}

fun ParadoxScriptComplexExpression.getReferences(element: ParadoxScriptExpressionElement): Array<PsiReference> {
	val references = SmartList<PsiReference>()
	this.doGetReferences(element, references)
	return references.toTypedArray()
}

private fun ParadoxScriptExpressionNode.doGetReferences(element: ParadoxScriptExpressionElement, references: SmartList<PsiReference>) {
	val reference = this.getReference(element)
	if(reference != null) {
		references.add(reference)
	}
	if(nodes.isNotEmpty()) {
		for(node in nodes) {
			node.doGetReferences(element, references)
		}
	}
}
