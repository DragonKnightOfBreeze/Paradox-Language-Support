package icu.windea.pls.script.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

/**
 * @param element [ParadoxArgument] | [ParadoxParameter]
 * @see icu.windea.pls.script.codeInsight.completion.ParadoxParameterCompletionProvider
 */
class ParadoxParameterPsiReference(
	element: PsiElement,
	rangeInElement: TextRange,
	private val read: Boolean
) : PsiReferenceBase<PsiElement>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		//重命名当前元素即可
		val element = element
		return when {
			element is ParadoxArgument -> element.setName(newElementName)
			element is ParadoxParameter -> element.setName(newElementName)
			else -> element
		}
	}
	
	override fun resolve(): PsiElement? {
		val element = element
		val name = rangeInElement.substring(element.text)
		//向上找到definition
		val definition = element.findParentDefinition() ?: return null
		val definitionInfo = definition.definitionInfo ?: return null
		return ParadoxParameterElement(element, name, definitionInfo.name, definitionInfo.type, definitionInfo.project, definitionInfo.gameType, read)
	}
}
