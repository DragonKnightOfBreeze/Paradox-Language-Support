package icu.windea.pls.script.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.annotations.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.impl.*

class ParadoxParameterReference(
	element: @UnionType(types = [ParadoxInputParameter::class, ParadoxParameter::class]) PsiElement,
	rangeInElement: TextRange,
	private val read: Boolean
) : PsiReferenceBase<PsiElement>(element, rangeInElement), ParadoxParameterResolvable {
	override fun handleElementRename(newElementName: String): PsiElement {
		//重命名引用指向的元素
		val element = element
		return when {
			element is ParadoxInputParameter -> element.setName(newElementName)
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
	
	/**
	 * @see icu.windea.pls.script.codeInsight.completion.ParadoxParameterCompletionProvider
	 */
	@Suppress("RedundantOverride")
	override fun getVariants(): Array<Any> {
		return super.getVariants() //not here
	}
}