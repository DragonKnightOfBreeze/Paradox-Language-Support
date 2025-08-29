package icu.windea.pls.lang.references.localisation

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.lang.util.ParadoxGameConceptManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * @see icu.windea.pls.lang.codeInsight.completion.localisation.ParadoxLocalisationConceptCompletionProvider
 */
class ParadoxLocalisationConceptPsiReference(
    element: ParadoxLocalisationConceptCommand,
    rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationConceptCommand>(element, rangeInElement) {
    val project by lazy { element.project }

    override fun handleElementRename(newElementName: String): PsiElement {
        //cannot rename when use alias
        if (element.name != resolve()?.name) throw IncorrectOperationException()
        return element.setName(rangeInElement.replace(element.text, newElementName))
    }

    //缓存解析结果以优化性能

    private object Resolver : ResolveCache.AbstractResolver<ParadoxLocalisationConceptPsiReference, ParadoxScriptDefinitionElement> {
        override fun resolve(ref: ParadoxLocalisationConceptPsiReference, incompleteCode: Boolean) = ref.doResolve()
    }

    override fun resolve(): ParadoxScriptDefinitionElement? {
        return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
    }

    private fun doResolve(): ParadoxScriptDefinitionElement? {
        val element = element
        val nameOrAlias = element.name
        return ParadoxGameConceptManager.get(nameOrAlias, project, element)
    }
}
