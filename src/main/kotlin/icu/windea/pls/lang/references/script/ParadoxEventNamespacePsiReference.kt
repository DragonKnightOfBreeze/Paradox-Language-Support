package icu.windea.pls.lang.references.script

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.core.createResults
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.manipulation.ParadoxEventManipulationService
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * （位于事件声明中的）事件ID中的事件命名空间引用。
 */
class ParadoxEventNamespacePsiReference(
    element: ParadoxScriptStringExpressionElement,
    rangeInElement: TextRange,
    val event: SmartPsiElementPointer<ParadoxScriptProperty>
) : PsiPolyVariantReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
    private val project get() = element.project

    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
    }

    override fun resolve(): PsiElement? {
        val element = element
        val event = event.element ?: return null
        val expectedNamespace = rangeInElement.substring(element.text)
        if (expectedNamespace.isEmpty()) return null

        // bound and matched
        val boundEventNamespaces = ParadoxEventManipulationService.getBoundNamespaceDeclarationsFromEventDeclaration(event)
        val boundEventNamespace = boundEventNamespaces.find { ParadoxEventManipulationService.getNamespaceFromEventNamespaceDeclaration(it) == expectedNamespace }
        if (boundEventNamespace != null) return boundEventNamespace

        val name = element.value.substringBefore('.')
        val selector = ParadoxDefinitionSearch.selector(project, event).contextSensitive()
        val eventNamespace = ParadoxDefinitionSearch.searchProperty(name, ParadoxDefinitionTypes.eventNamespace, selector).find()
        return eventNamespace
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        val element = element
        val event = event.element ?: return ResolveResult.EMPTY_ARRAY
        val expectedNamespace = rangeInElement.substring(element.text)
        if (expectedNamespace.isEmpty()) return ResolveResult.EMPTY_ARRAY
        val result = mutableSetOf<PsiElement>()

        // bound and matched
        val boundEventNamespaces = ParadoxEventManipulationService.getBoundNamespaceDeclarationsFromEventDeclaration(event)
        val boundEventNamespace = boundEventNamespaces.filter { ParadoxEventManipulationService.getNamespaceFromEventNamespaceDeclaration(it) == expectedNamespace }
        result.addAll(boundEventNamespace)

        val name = element.value.substringBefore('.')
        val selector = ParadoxDefinitionSearch.selector(project, event).contextSensitive()
        val eventNamespaces = ParadoxDefinitionSearch.searchProperty(name, ParadoxDefinitionTypes.eventNamespace, selector).findAll()
        result.addAll(eventNamespaces)

        return result.createResults()
    }
}
