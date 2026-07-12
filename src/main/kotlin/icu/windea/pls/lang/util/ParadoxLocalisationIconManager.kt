package icu.windea.pls.lang.util

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.resolve.ParadoxLocalisationIconService
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

object ParadoxLocalisationIconManager {
    fun getFrame(element: ParadoxLocalisationIcon): Int {
        val argumentElement = element.argumentElement ?: return 0 // no argument element -> return 0
        val idElement = argumentElement.idElement ?: return 0 // contain parameters or commands -> return 0
        return idElement.text.toIntOrNull() ?: 0
    }

    /**
     * @see ParadoxLocalisationIconService.resolve
     */
    fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement? {
        ProgressManager.checkCanceled()
        return ParadoxLocalisationIconService.resolve(name, element, project)
    }

    /**
     * @see ParadoxLocalisationIconService.resolveAll
     */
    fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement> {
        ProgressManager.checkCanceled()
        return ParadoxLocalisationIconService.resolveAll(name, element, project)
    }

    /**
     * @see ParadoxLocalisationIconService.complete
     */
    fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        return ParadoxLocalisationIconService.complete(context, result)
    }
}
