package icu.windea.pls.lang.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.ep.parameter.*

class ParadoxParameterPsiReference(
	element: ParadoxParameter,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxParameter>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		return ParadoxParameterSupport.resolveParameter(element)
	}
}