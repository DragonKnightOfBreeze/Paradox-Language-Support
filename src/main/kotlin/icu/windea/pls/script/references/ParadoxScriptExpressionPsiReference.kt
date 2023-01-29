package icu.windea.pls.script.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.script.codeInsight.completion.ParadoxDefinitionCompletionProvider
 */
class ParadoxScriptExpressionPsiReference(
	element: ParadoxScriptExpressionElement,
	rangeInElement: TextRange,
	val config: CwtDataConfig<*>,
	val isKey: Boolean
) : PsiPolyVariantReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement), PsiNodeReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		val element = element
		return when {
			element is ParadoxScriptStringExpressionElement -> element.setValue(newElementName)
			element is ParadoxScriptInt -> element.setValue(newElementName)
			else -> throw IncorrectOperationException()
		}
	}
	
	override fun isReferenceTo(element: PsiElement): Boolean {
		//必要的处理，否则查找使用时会出现问题（输入的PsiElement永远不会是propertyKey）
		//直接调用resolve()即可
		val resolved = resolve(false)
		val manager = element.manager
		return manager.areElementsEquivalent(resolved, element) || (resolved is ParadoxScriptProperty && manager.areElementsEquivalent(resolved.propertyKey, element))
	}
	
	override fun resolve(): PsiElement? {
		return resolve(true)
	}
	
	override fun resolve(exact: Boolean): PsiElement? {
		//根据对应的expression进行解析
		return CwtConfigHandler.resolveScriptExpression(element, rangeInElement, config, config.expression, config.info.configGroup, isKey, exact = exact)
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		//根据对应的expression进行解析
		return CwtConfigHandler.multiResolveScriptExpression(element, rangeInElement, config, config.expression, config.info.configGroup, isKey)
			.mapToArray { PsiElementResolveResult(it) }
	}
}

