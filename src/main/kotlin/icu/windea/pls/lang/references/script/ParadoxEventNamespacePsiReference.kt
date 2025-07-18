package icu.windea.pls.lang.references.script

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

class ParadoxEventNamespacePsiReference(
    element: ParadoxScriptString,
    rangeInElement: TextRange,
    val event: SmartPsiElementPointer<ParadoxScriptProperty>
) : PsiPolyVariantReferenceBase<ParadoxScriptString>(element, rangeInElement) {
    val project by lazy { element.project }

    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
    }

    override fun resolve(): PsiElement? {
        val element = element
        val event = event.element ?: return null
        //val definitionInfo = event.definitionInfo ?: return null
        val preferredEventNamespace = ParadoxEventManager.getMatchedNamespace(event)
        if (preferredEventNamespace != null) return preferredEventNamespace

        val name = element.value.substringBefore('.')
        val selector = selector(project, event).definition().contextSensitive()
        val eventNamespace = ParadoxDefinitionSearch.search(name, "event_namespace", selector).find()
        return eventNamespace
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        val element = element
        val event = event.element ?: return ResolveResult.EMPTY_ARRAY
        //val definitionInfo = event.definitionInfo ?: return ResolveResult.EMPTY_ARRAY
        val result = mutableSetOf<PsiElement>()
        val preferredEventNamespace = ParadoxEventManager.getMatchedNamespace(event)
        if (preferredEventNamespace != null) result.add(preferredEventNamespace)

        val name = element.value.substringBefore('.')
        val selector = selector(project, event).definition().contextSensitive()
        val eventNamespaces = ParadoxDefinitionSearch.search(name, "event_namespace", selector).findAll()
        result.addAll(eventNamespaces)
        return result.mapToArray { PsiElementResolveResult(it) }
    }
}
