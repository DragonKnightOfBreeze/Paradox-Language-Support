package icu.windea.pls.script.codeInsight.highghting

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*
import icu.windea.pls.script.reference.*

/**
 * 在查找使用中，区分参数和值集中的值的读/写使用
 */
class ParadoxScriptReadWriteAccessDetector : ReadWriteAccessDetector() {
	override fun isReadWriteAccessible(element: PsiElement): Boolean {
		return when {
			element is ParadoxParameterElement -> true
			element is ParadoxValueSetValueElement -> true
			else -> false
		}
	}
	
	override fun isDeclarationWriteAccess(element: PsiElement): Boolean {
		return when {
			element is ParadoxParameterElement -> true
			element is ParadoxValueSetValueElement -> true
			else -> false
		}
	}
	
	override fun getReferenceAccess(referencedElement: PsiElement, reference: PsiReference): Access {
		return when{
			reference is ParadoxParameterResolvable -> reference.resolveSingle()
				?.castOrNull<ParadoxParameterElement>()?.getAccess() ?: Access.ReadWrite
			reference is ParadoxValueSetValueResolvable -> reference.resolveSingle()
				?.castOrNull<ParadoxValueSetValueElement>()?.getAccess() ?: Access.ReadWrite
			else -> Access.ReadWrite
		}
	}
	
	override fun getExpressionAccess(expression: PsiElement): Access {
		//find usages use this method finally
		if(expression.language != ParadoxScriptLanguage) return Access.ReadWrite
		for(reference in expression.references) {
			ProgressManager.checkCanceled()
			when(reference){
				is ParadoxParameterResolvable -> {
					val resolved = reference.resolveSingle()?.castOrNull<ParadoxParameterElement>() ?: continue
					return resolved.getAccess()
				}
				is ParadoxValueSetValueResolvable -> {
					val resolved = reference.resolveSingle()?.castOrNull<ParadoxValueSetValueElement>() ?: continue
					return resolved.getAccess()
				}
			}
		}
		return Access.ReadWrite
	}
	
	private fun ParadoxValueSetValueElement.getAccess() = if(read) Access.Read else Access.Write
	
	private fun ParadoxParameterElement.getAccess() = if(read) Access.Read else Access.Write
}