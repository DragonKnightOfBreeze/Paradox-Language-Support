package icu.windea.pls.script.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.script.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.script.codeInsight.completion.ParadoxEventIdCompletionProvider
 */
class ParadoxEventNamespacePsiReference(
	element: ParadoxScriptString,
	rangeInElement: TextRange,
	val event: SmartPsiElementPointer<ParadoxScriptProperty>
): PsiPolyVariantReferenceBase<ParadoxScriptString>( element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setValue(rangeInElement.replace(element.value, newElementName))
	}
	
	override fun resolve(): PsiElement? {
		val element = element
		val event = event.element ?: return null
		val definitionInfo = event.definitionInfo ?: return null
		val preferredEventNamespace = ParadoxEventHandler.getEventNamespace(event)
		if(preferredEventNamespace != null) return preferredEventNamespace
		
		val gameType = definitionInfo.gameType
		val project = definitionInfo.project
		val name = element.value.substringBefore('.')
		val selector = definitionSelector().gameType(gameType).preferRootFrom(event)
		val query = ParadoxDefinitionSearch.search(name, "event_namespace", project, selector = selector)
		val eventNamespace = query.find()
		return eventNamespace
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {		
		val element = element
		val event = event.element ?: return ResolveResult.EMPTY_ARRAY
		val definitionInfo = event.definitionInfo ?: return ResolveResult.EMPTY_ARRAY
		val result = mutableSetOf<PsiElement>()
		val preferredEventNamespace = ParadoxEventHandler.getEventNamespace(event)
		if(preferredEventNamespace != null) result.add(preferredEventNamespace)
		
		val gameType = definitionInfo.gameType
		val project = definitionInfo.project
		val name = element.value.substringBefore('.')
		val selector = definitionSelector().gameType(gameType).preferRootFrom(event)
		val query = ParadoxDefinitionSearch.search(name, "event_namespace", project, selector = selector)
		val eventNamespaces = query.findAll()
		result.addAll(eventNamespaces)
		return result.mapToArray { PsiElementResolveResult(it) }
	}
}