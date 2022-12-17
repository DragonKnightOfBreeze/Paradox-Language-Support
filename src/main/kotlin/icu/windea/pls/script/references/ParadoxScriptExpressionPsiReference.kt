package icu.windea.pls.script.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.definition.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.script.codeInsight.completion.ParadoxDefinitionCompletionProvider
 */
class ParadoxScriptExpressionPsiReference(
	element: ParadoxScriptStringExpressionElement,
	rangeInElement: TextRange,
	val config: CwtDataConfig<*>,
	val isKey: Boolean
) : PsiPolyVariantReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement), SmartPsiReference {
	override fun handleElementRename(newElementName: String): PsiElement {
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
		return resolve(true)
	}
	
	override fun resolve(exact: Boolean): PsiElement? {
		//根据对应的expression进行解析
		return CwtConfigHandler.resolveScriptExpression(element, rangeInElement, config, config.info.configGroup, isKey, exact = exact)
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		//根据对应的expression进行解析
		return CwtConfigHandler.multiResolveScriptExpression(element, rangeInElement, config, config.info.configGroup, isKey)
			.mapToArray { PsiElementResolveResult(it) }
	}
}

class ParadoxEventNamespacePsiReference(
	element: ParadoxScriptString,
	rangeInElement: TextRange,
	val config: CwtDataConfig<*>
): PsiPolyVariantReferenceBase<ParadoxScriptString> ( element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setValue(rangeInElement.replace(element.value, newElementName))
	}
	
	override fun resolve(): PsiElement? {
		val gameType = config.info.configGroup.gameType ?: return null
		val event = element.parent.castOrNull<ParadoxScriptProperty>()
		DefinitionConfigHandler.getEventNamespace()
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		val gameType = config.info.configGroup.gameType ?: return ResolveResult.EMPTY_ARRAY
		val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
		return 
	}
}