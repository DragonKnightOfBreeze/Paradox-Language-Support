package icu.windea.pls.ep.resolve.localisation

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.annotations.WithGameTypeEP
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
    }
}
