package icu.windea.pls.core.codeInsight.highlight

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*
import icu.windea.pls.script.expression.reference.*

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
		if(reference.canResolveParameter()) {
			val resolved = reference.resolveSingle()?.castOrNull<ParadoxParameterElement>()
			if(resolved != null) return resolved.getAccess()
		}
		if(reference.canResolveValueSetValue()) {
			val resolved = reference.resolveSingle()?.castOrNull<ParadoxValueSetValueElement>()
			if(resolved != null) return resolved.getAccess()
		} 
		return Access.ReadWrite
	}
	
	override fun getExpressionAccess(expression: PsiElement): Access {
		//find usages use this method finally
		if(expression.language != ParadoxScriptLanguage && expression.language != ParadoxLocalisationLanguage) return Access.ReadWrite
		for(reference in expression.references) {
			ProgressManager.checkCanceled()
			if(reference.canResolveParameter()) {
				val resolved = reference.resolveSingle()?.castOrNull<ParadoxParameterElement>()
				if(resolved != null) return resolved.getAccess()
			}
			if(reference.canResolveValueSetValue()) {
				val resolved = reference.resolveSingle()?.castOrNull<ParadoxValueSetValueElement>()
				if(resolved != null) return resolved.getAccess()
			}
		}
		return Access.ReadWrite
	}
	
	private fun ParadoxValueSetValueElement.getAccess() = if(read) Access.Read else Access.Write
	
	private fun ParadoxParameterElement.getAccess() = if(read) Access.Read else Access.Write
}