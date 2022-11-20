package icu.windea.pls.script.exp

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
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

fun ParadoxScriptComplexExpression.annotate(element: ParadoxScriptExpressionElement, range: TextRange, holder: AnnotationHolder) {
	this.doAnnotate(element, range, holder)
}

private fun ParadoxScriptExpressionNode.doAnnotate(element: ParadoxScriptExpressionElement, range: TextRange, holder: AnnotationHolder) {
	val attributesKey = this.getAttributesKey()
	if(attributesKey != null) {
		if(this is ParadoxScriptTokenExpressionNode) {
			//enforce text attributes when node is a token
			holder.newSilentAnnotation(INFORMATION).range(rangeInExpression.shiftRight(range.startOffset))
				.enforcedTextAttributes(EditorColorsManager.getInstance().schemeForCurrentUITheme.getAttributes(attributesKey))
				.create()
		} else {
			holder.newSilentAnnotation(INFORMATION).range(rangeInExpression.shiftRight(range.startOffset))
				.textAttributes(attributesKey)
				.create()
		}
	} else {
		val resolvedTextAttributesKey = this.getReference(element)?.castOrNull<SmartPsiReference>()?.resolveTextAttributesKey()
		if(resolvedTextAttributesKey != null) {
			holder.newSilentAnnotation(INFORMATION).range(rangeInExpression.shiftRight(range.startOffset))
				.textAttributes(resolvedTextAttributesKey)
				.create()
		}
	}
	if(nodes.isNotEmpty()) {
		for(node in nodes) {
			node.doAnnotate(element, range, holder)
		}
	}
}

