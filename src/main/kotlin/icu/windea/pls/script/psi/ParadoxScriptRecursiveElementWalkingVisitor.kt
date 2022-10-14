package icu.windea.pls.script.psi

import com.intellij.openapi.progress.*
import com.intellij.psi.*

abstract class ParadoxScriptRecursiveElementWalkingVisitor : ParadoxScriptVisitor(), PsiRecursiveVisitor {
	private val walkingState: PsiWalkingState = object : PsiWalkingState(this) {
		override fun elementFinished(element: PsiElement) {
			this@ParadoxScriptRecursiveElementWalkingVisitor.elementFinished(element)
		}
	}
	
	override fun visitElement(element: PsiElement) {
		ProgressManager.checkCanceled()
		walkingState.elementStarted(element)
	}
	
	protected open fun elementFinished(element: PsiElement?) {}
	
	fun stopWalking() {
		walkingState.stopWalking()
	}
}

abstract class ParadoxScriptRecursiveExpressionElementWalkingVisitor : ParadoxScriptRecursiveElementWalkingVisitor() {
	final override fun visitBoolean(o: ParadoxScriptBoolean) {}
	final override fun visitInt(o: ParadoxScriptInt) {}
	final override fun visitFloat(o: ParadoxScriptFloat) {}
	final override fun visitColor(o: ParadoxScriptColor) {}
	final override fun visitInlineMath(o: ParadoxScriptInlineMath) {}
	final override fun visitParameter(o: ParadoxScriptParameter) {}
	final override fun visitParadoxInputParameter(o: ParadoxInputParameter) {}
	final override fun visitParameterCondition(o: ParadoxScriptParameterCondition) {}
	final override fun visitVariable(o: ParadoxScriptVariable) {}
	
	override fun visitExpressionElement(element: ParadoxScriptExpressionElement) {
		
	}
	
	override fun visitPropertyKey(element: ParadoxScriptPropertyKey) {
		visitExpressionElement(element)
	}
	
	override fun visitString(element: ParadoxScriptString) {
		visitExpressionElement(element)
	}
}