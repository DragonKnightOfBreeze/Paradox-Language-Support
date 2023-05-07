package icu.windea.pls.core.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.parameter.*

class ParadoxArgumentPsiReference(
	element: ParadoxConditionParameter,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxConditionParameter>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		//重命名当前元素
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		return ParadoxParameterSupport.resolveConditionParameter(element)
	}
}
