package icu.windea.pls.lang.resolve

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.ep.resolve.localisation.ParadoxLocalisationIconSupport
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.codeInsight.completion.gameType
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

object ParadoxLocalisationIconService {
    /**
     * @see ParadoxLocalisationIconSupport.resolve
     */
    fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement? {
        val gameType = selectGameType(element)
        return ParadoxLocalisationIconSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ProgressManager.checkCanceled()
            ep.resolve(name, element, project)
        }
    }

    /**
     * @see ParadoxLocalisationIconSupport.resolveAll
     */
    fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement> {
        val gameType = selectGameType(element)
        return ParadoxLocalisationIconSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ProgressManager.checkCanceled()
            ep.resolveAll(name, element, project).orNull()
        }.orEmpty()
    }

    /**
     * @see ParadoxLocalisationIconSupport.complete
     */
    fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val gameType = context.gameType
        ParadoxLocalisationIconSupport.EP_NAME.extensionList.forEach f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            ProgressManager.checkCanceled()
            ep.complete(context, result)
        }
    }
}

