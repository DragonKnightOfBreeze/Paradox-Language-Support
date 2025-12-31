package icu.windea.pls.lang.references.script

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import icu.windea.pls.core.createResults
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey

class ParadoxDefinitionInjectionTargetPsiReference(
    element: ParadoxScriptPropertyKey,
    rangeInElement: TextRange,
    val info: ParadoxDefinitionInjectionInfo,
) : PsiPolyVariantReferenceBase<ParadoxScriptPropertyKey>(element, rangeInElement) {
    private val project get() = element.project

    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
    }

    override fun resolve(): PsiElement? {
        val element = element
        val name = info.target
        val type = info.type
        val selector = selector(project, element).definition().contextSensitive()
        val resolved = ParadoxDefinitionSearch.search(name, type, selector).find()
        return resolved
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult?> {
        val element = element
        val name = info.target
        val type = info.type
        val selector = selector(project, element).definition().contextSensitive()
        val resolved = ParadoxDefinitionSearch.search(name, type, selector).findAll()
        return resolved.createResults()
    }
}
