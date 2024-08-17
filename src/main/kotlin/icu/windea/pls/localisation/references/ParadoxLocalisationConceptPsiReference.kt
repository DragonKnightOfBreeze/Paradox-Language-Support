package icu.windea.pls.localisation.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import com.intellij.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationConceptCompletionProvider
 */
class ParadoxLocalisationConceptPsiReference(
    element: ParadoxLocalisationConcept,
    rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationConcept>(element, rangeInElement) {
    val project by lazy { element.project }
    
    override fun handleElementRename(newElementName: String): PsiElement {
        //cannot rename when use alias
        if(resolve()?.name != element.name) throw IncorrectOperationException()
        return element.setName(rangeInElement.replace(element.text, newElementName))
    }
    
    //缓存解析结果以优化性能
    
    private object Resolver : ResolveCache.AbstractResolver<ParadoxLocalisationConceptPsiReference, ParadoxScriptDefinitionElement> {
        override fun resolve(ref: ParadoxLocalisationConceptPsiReference, incompleteCode: Boolean): ParadoxScriptDefinitionElement? {
            return ref.doResolve()
        }
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
