package icu.windea.pls.core.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.lang.support.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.support.*

/**
 * @see icu.windea.pls.script.codeInsight.completion.ParadoxParameterCompletionProvider
 */
class ParadoxParameterPsiReference(
	element: ParadoxParameter,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxParameter>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		//重命名当前元素
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		val element = element
		val name = rangeInElement.substring(element.text)
		return ParadoxParameterResolver.resolveParameter(name, element)
	}
}