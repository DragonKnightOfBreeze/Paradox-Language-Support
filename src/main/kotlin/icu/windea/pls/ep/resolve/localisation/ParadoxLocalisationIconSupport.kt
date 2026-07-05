package icu.windea.pls.ep.resolve.localisation

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.base.annotations.WithGameTypeEP
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

/**
 * 提供对本地化图标的支持。
 */
@WithGameTypeEP
interface ParadoxLocalisationIconSupport {
    fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement?

    fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement>

    fun complete(context: ParadoxCompletionContext, result: CompletionResultSet)

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxLocalisationIconSupport>("icu.windea.pls.localisationIconSupport")
    }
}
