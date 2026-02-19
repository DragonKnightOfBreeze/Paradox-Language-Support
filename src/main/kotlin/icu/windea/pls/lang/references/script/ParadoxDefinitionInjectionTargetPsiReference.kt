package icu.windea.pls.lang.references.script

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import icu.windea.pls.core.createResults
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
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
        if (info.target.isNullOrEmpty() || info.type.isNullOrEmpty()) return null
        val selector = selector(project, element).definition().contextSensitive()
        val resolved = ParadoxDefinitionSearch.searchElement(info.target, info.type, selector).find()
        return resolved
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        if (info.target.isNullOrEmpty() || info.type.isNullOrEmpty()) return ResolveResult.EMPTY_ARRAY
        val selector = selector(project, element).definition().contextSensitive()
        val resolved = ParadoxDefinitionSearch.searchElement(info.target, info.type, selector).findAll()
        return resolved.createResults()
    }
}
