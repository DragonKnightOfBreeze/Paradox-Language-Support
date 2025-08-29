package icu.windea.pls.ep.icon

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.annotations.WithGameTypeEP
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.lang.codeInsight.completion.gameType
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.supportsByAnnotation
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

/**
 * 提供对本地化图标的支持。
 */
@WithGameTypeEP
interface ParadoxLocalisationIconSupport {
    fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement?

    fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement>

    fun complete(context: ProcessingContext, result: CompletionResultSet)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxLocalisationIconSupport>("icu.windea.pls.localisationIconSupport")

        fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement? {
            val gameType = selectGameType(element)
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                ProgressManager.checkCanceled()
                ep.resolve(name, element, project)
            }
        }

        fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement> {
            val gameType = selectGameType(element)
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                ProgressManager.checkCanceled()
                ep.resolveAll(name, element, project).orNull()
            }.orEmpty()
        }

        fun complete(context: ProcessingContext, result: CompletionResultSet) {
            val gameType = context.gameType
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f
                ProgressManager.checkCanceled()
                ep.complete(context, result)
            }
        }
    }
}
