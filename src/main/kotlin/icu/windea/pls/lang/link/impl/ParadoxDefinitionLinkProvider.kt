package icu.windea.pls.lang.link.impl

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.link.*

class ParadoxDefinitionLinkProvider : PsiElementLinkProvider {
    //e.g. #definition/stellaris/civic_or_origin.origin/origin_default
    
    companion object {
        const val LINK_PREFIX = "#definition/"
    }
    
    override val linkPrefix = LINK_PREFIX
    
    override fun resolveLink(shortLink: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val tokens = shortLink.split('/')
        if(tokens.size > 3) return null
        val typeExpression = tokens.getOrNull(1) ?: return null
        val name = tokens.getOrNull(2) ?: return null
        val project = contextElement.project
        val selector = definitionSelector(project, contextElement).contextSensitive()
        return ParadoxDefinitionSearch.search(name, typeExpression, selector).find()
    }
}
