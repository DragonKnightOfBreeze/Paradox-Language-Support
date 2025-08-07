package icu.windea.pls.ep.icon

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.extensions.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.localisation.psi.*

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
