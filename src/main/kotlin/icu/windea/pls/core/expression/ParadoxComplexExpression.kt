package icu.windea.pls.core.expression

import com.intellij.codeInsight.completion.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.script.psi.*

interface ParadoxComplexExpression : ParadoxExpression, ParadoxExpressionNode {
	val configGroup: CwtConfigGroup
	
	fun validate(): List<ParadoxExpressionError> = emptyList()
	
	fun complete(context: ProcessingContext, result: CompletionResultSet) = pass()
}

fun ParadoxComplexExpression.processAllNodes(processor: Processor<ParadoxExpressionNode>): Boolean {
	return doProcessAllNodes(processor)
}

private fun ParadoxExpressionNode.doProcessAllNodes(processor: Processor<ParadoxExpressionNode>): Boolean {
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

fun ParadoxComplexExpression.processAllLeafNodes(processor: Processor<ParadoxExpressionNode>): Boolean {
	return doProcessAllLeafNodes(processor)
}

private fun ParadoxExpressionNode.doProcessAllLeafNodes(processor: Processor<ParadoxExpressionNode>): Boolean {
	if(nodes.isNotEmpty()) {
		for(node in nodes) {
			val r1 = node.doProcessAllLeafNodes(processor)
			if(!r1) return false
		}
		return true
	} else {
		return processor.process(this)
	}
}

fun ParadoxComplexExpression.getReferences(element: ParadoxScriptStringExpressionElement): Array<PsiReference> {
	val references = SmartList<PsiReference>()
	this.doGetReferences(element, references)
	return references.toTypedArray()
}

private fun ParadoxExpressionNode.doGetReferences(element: ParadoxScriptStringExpressionElement, references: SmartList<PsiReference>) {
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