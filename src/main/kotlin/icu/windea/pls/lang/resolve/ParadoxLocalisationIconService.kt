package icu.windea.pls.lang.resolve

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.ep.resolve.localisation.ParadoxLocalisationIconSupport
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

@Optimized
object ParadoxLocalisationIconService {
    /**
     * @see ParadoxLocalisationIconSupport.resolve
     */
    fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement? {
        val gameType = selectGameType(element)
        val supports = ParadoxLocalisationIconSupport.EP_NAME.extensionList
        supports.forEachFast f@{ support ->
            if (gameType != null && !support.supports(gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            support.resolve(name, element, project)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxLocalisationIconSupport.resolveAll
     */
    fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement> {
        val gameType = selectGameType(element)
        val supports = ParadoxLocalisationIconSupport.EP_NAME.extensionList
        supports.forEachFast f@{ support ->
            if (gameType != null && !support.supports(gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            support.resolveAll(name, element, project).orNull()?.let { return it }
        }
        return emptyList()
    }

    /**
     * @see ParadoxLocalisationIconSupport.complete
     */
    fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val gameType = context.gameType
        val supports = ParadoxLocalisationIconSupport.EP_NAME.extensionList
        supports.forEachFast f@{ support ->
            if (!support.supports(gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            support.complete(context, result)
        }
    }
}

