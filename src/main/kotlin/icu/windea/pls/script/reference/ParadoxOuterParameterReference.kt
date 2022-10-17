package icu.windea.pls.script.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.annotations.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.impl.*

class ParadoxOuterParameterReference(
	element: @UnionType(types = [ParadoxScriptExpressionElement::class]) PsiElement,
	rangeInElement: TextRange,
	private val definitionName: String,
	private val definitionType: String,
	private val configGroup: CwtConfigGroup
) : PsiReferenceBase<PsiElement>(element, rangeInElement), ParadoxParameterResolvable {
	override fun handleElementRename(newElementName: String): PsiElement {
		//重命名引用指向的元素（在对应的范围内）
		val element = element
		return when {
			element is ParadoxScriptExpressionElement -> element.setValue(rangeInElement.replace(element.value, newElementName))
			else -> element
		}
	}
	
	override fun resolve(): PsiElement {
		val element = element
		val name = rangeInElement.substring(element.text)
		return ParadoxParameterElement(element, name, definitionName, definitionType, configGroup.project, configGroup.gameType, false)
	}
}