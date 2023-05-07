package icu.windea.pls.core.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

class ParadoxArgumentValuePsiReference(
	element: PsiElement,
	rangeInElement: TextRange,
	private val parameterResolver: () -> ParadoxParameterElement?
): PsiReferenceBase<PsiElement>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		val element = element
		return when {
			element is ParadoxScriptStringExpressionElement -> element.setValue(rangeInElement.replace(element.value, newElementName))
			else -> throw IncorrectOperationException()
		}
	}
	
	override fun resolve(): PsiElement? {
		val parameter = parameterResolver() ?: return null
		//TODO 0.9.16
		return null
	}
}