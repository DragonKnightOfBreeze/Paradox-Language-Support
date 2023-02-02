package icu.windea.pls.core.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.core.component.*
import icu.windea.pls.core.psi.*

/**
 * @see icu.windea.pls.script.codeInsight.completion.ParadoxParameterCompletionProvider
 */
class ParadoxArgumentPsiReference(
	element: ParadoxArgument,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxArgument>(element, rangeInElement) {
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
