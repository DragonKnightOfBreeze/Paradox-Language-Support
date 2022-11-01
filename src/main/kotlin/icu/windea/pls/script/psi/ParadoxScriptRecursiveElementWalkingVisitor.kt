package icu.windea.pls.script.psi

import com.intellij.psi.*

abstract class ParadoxScriptRecursiveElementWalkingVisitor : ParadoxScriptVisitor(), PsiRecursiveVisitor {
	private val walkingState: PsiWalkingState = object : PsiWalkingState(this) {
		override fun elementFinished(element: PsiElement) {
			this@ParadoxScriptRecursiveElementWalkingVisitor.elementFinished(element)
		}
	}
	
	override fun visitElement(element: PsiElement) {
		walkingState.elementStarted(element)
	}
	
	protected open fun elementFinished(element: PsiElement?) {}
	
	fun stopWalking() {
		walkingState.stopWalking()
	}
}

