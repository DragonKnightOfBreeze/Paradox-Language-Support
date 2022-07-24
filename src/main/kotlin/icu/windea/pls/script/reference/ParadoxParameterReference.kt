package icu.windea.pls.script.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.annotations.*
import icu.windea.pls.script.psi.*

class ParadoxParameterReference(
	element: @UnionType(IParadoxScriptParameter::class, IParadoxScriptInputParameter::class) PsiElement,
	rangeInElement: TextRange
) : PsiReferenceBase<PsiElement>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		//重命名当前parameter/inputParameter以及解析引用得到的那个parameter
		val resolved = resolve()
		when {
			resolved == null -> pass()
			resolved is PsiNamedElement -> resolved.setName(newElementName)
		}
		val element = element
		return when {
			element is PsiNamedElement -> element.setName(newElementName)
			else -> element
		}
	}
	
	override fun resolve(): PsiElement? {
		//向上找到definition，然后找到其中同名的首个parameter（可以为自身）
		val element = element
		val name = when{
			element is IParadoxScriptParameter -> element.name
			element is IParadoxScriptInputParameter -> element.name
			else -> return null
		}
		val definition = element.findParentDefinition() ?: return null
		return definition.parameterMap[name]?.firstNotNullOfOrNull { it.element }
	}
}