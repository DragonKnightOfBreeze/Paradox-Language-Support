package icu.windea.pls.lang.link.impl

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.link.*

class ParadoxFilePathLinkProvider: PsiElementLinkProvider {
    //e.g. #path/stellaris/path
    
    companion object {
        const val LINK_PREFIX = "#path/"
    }
    
    override val linkPrefix = LINK_PREFIX
    
    override fun resolveLink(shortLink: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val tokens = shortLink.split('/', limit = 2)
        val filePath = tokens.getOrNull(1) ?: return null
        val project = contextElement.project
        val selector = fileSelector(project, contextElement).contextSensitive()
        return ParadoxFilePathSearch.search(filePath, null, selector).find()
            ?.toPsiFile(project)
    }
}
