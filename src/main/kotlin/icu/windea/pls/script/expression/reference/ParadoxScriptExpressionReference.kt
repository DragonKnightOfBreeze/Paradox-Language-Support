package icu.windea.pls.script.expression.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.script.codeInsight.completion.ParadoxDefinitionCompletionProvider
 */
class ParadoxScriptExpressionReference(
	element: ParadoxScriptExpressionElement,
	rangeInElement: TextRange,
	val config: CwtDataConfig<*>,
	val isKey: Boolean
) : PsiPolyVariantReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		//尝试重命名关联的definition、localisation、syncedLocalisation等
		val resolved = resolve()
		when {
			resolved == null -> pass()
			resolved.language == CwtLanguage -> throw IncorrectOperationException() //不允许重命名
			resolved is PsiFile -> resolved.setNameWithoutExtension(newElementName)
			resolved is PsiNamedElement -> resolved.setName(newElementName)
			else -> throw IncorrectOperationException() //不允许重命名
		}
		//重命名当前元素
		return element.setValue(newElementName)
	}
	
	override fun isReferenceTo(element: PsiElement): Boolean {
		//必要的处理，否则查找使用时会出现问题（输入的PsiElement永远不会是propertyKey）
		val results = multiResolve(false)
		val manager = getElement().manager
		for(result in results) {
			val resolved = result.element
			if(manager.areElementsEquivalent(resolved, element) || (resolved is ParadoxScriptProperty && manager.areElementsEquivalent(resolved.propertyKey, element))) {
				return true
			}
		}
		return false
	}
	
	override fun resolve(): PsiElement? {
		return CwtConfigHandler.resolveScriptExpression(element, rangeInElement, config.expression, config, isKey) //根据对应的expression进行解析
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		return CwtConfigHandler.multiResolveScriptExpression(element, rangeInElement, config.expression, config, isKey)
			.mapToArray { PsiElementResolveResult(it) } //根据对应的expression进行解析
	}
}
