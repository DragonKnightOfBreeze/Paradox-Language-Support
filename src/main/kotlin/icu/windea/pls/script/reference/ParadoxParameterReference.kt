package icu.windea.pls.script.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.annotations.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.util.selector.*

/**
 * @property svName 如果对应SV表达式中的参数引用，则为SV的名字。
 */
class ParadoxParameterReference(
	element: @UnionType(IParadoxScriptParameter::class, IParadoxScriptInputParameter::class) PsiElement,
	rangeInElement: TextRange,
	private val svName: String? = null
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
		val name = rangeInElement.substring(element.text)
		if(svName != null){
			//这里解析得到的SV和SV表达式中通过SV名字解析得到的SV可能不是同一个，会尝试解析成有这个名字的参数的SV
			val selector = definitionSelector().gameTypeFrom(element).preferRootFrom(element)
			val svList = findDefinitions(svName, "script_value", element.project, selector = selector)
			return svList.firstNotNullOfOrNull { sv ->
				sv.parameterMap[name]?.firstNotNullOfOrNull { it.element }
			}
		} else {
			val definition = element.findParentDefinition() ?: return null
			return definition.parameterMap[name]?.firstNotNullOfOrNull { it.element }
		}
	}
}