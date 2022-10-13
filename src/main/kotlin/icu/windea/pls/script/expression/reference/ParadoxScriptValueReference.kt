package icu.windea.pls.script.expression.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.cwt.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueReference(
	element: ParadoxScriptString,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxScriptString>(element, rangeInElement), PsiPolyVariantReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//尝试重命名关联的definition、localisation、syncedLocalisation等
		val resolved = resolve()
		when {
			resolved == null -> pass()
			resolved.language == CwtLanguage -> throw IncorrectOperationException() //不允许重命名
			resolved is PsiNamedElement -> resolved.setName(newElementName)
			else -> throw IncorrectOperationException() //不允许重命名
		}
		//重命名引用指向的元素
		return element.setValue(newElementName)
	}
	
	override fun isReferenceTo(element: PsiElement): Boolean {
		//必要的处理
		val resolved = resolve()
		val manager = getElement().manager
		return manager.areElementsEquivalent(resolved, element) || (resolved is ParadoxScriptProperty && manager.areElementsEquivalent(resolved.propertyKey, element))
	}
	
	override fun resolve(): PsiElement? {
		return CwtConfigHandler.resolveValue(element) //根据对应的expression进行解析
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		return CwtConfigHandler.multiResolveValue(element).mapToArray { PsiElementResolveResult(it) } //根据对应的expression进行解析
	}
	
	/**
	 * 由[icu.windea.pls.script.codeInsight.completion.ParadoxDefinitionCompletionProvider]提供代码补全。
	 */
	override fun getVariants(): Array<Any> {
		return ArrayUtilRt.EMPTY_OBJECT_ARRAY
	}
}