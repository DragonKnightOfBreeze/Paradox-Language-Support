package icu.windea.pls.lang.link.impl

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.link.*

class ParadoxLocalisationLinkProvider : PsiElementLinkProvider {
    //e.g. #localisation/stellaris/KEY
    
    companion object {
        const val LINK_PREFIX = "#localisation/"
    }
    
    override val linkPrefix = LINK_PREFIX
    
    override fun resolveLink(shortLink: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val tokens = shortLink.split('/')
        if(tokens.size > 2) return null
        val name = tokens.getOrNull(1) ?: return null
        val project = contextElement.project
        val selector = localisationSelector(project, contextElement).contextSensitive().preferLocale(contextElement.localeConfig)
        return ParadoxLocalisationSearch.search(name, selector).find()
    }
}