package icu.windea.pls.lang.codeInsight.documentation

import com.intellij.psi.PsiElement
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.forEachReversedFast
import icu.windea.pls.core.orNull
import icu.windea.pls.ep.codeInsight.documentation.ParadoxQuickDocTextProvider
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.orSpecific

object ParadoxDocumentationService {
    /**
     * @see ParadoxQuickDocTextProvider.getQuickDocText
     */
    @Suppress("unused")
    fun getQuickDocText(element: PsiElement): String? {
        val gameType = selectGameType(element)
        val eps = ParadoxQuickDocTextProvider.EP_NAME.extensionList
        eps.forEachReversedFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ep.getQuickDocText(element)?.orNull()?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxQuickDocTextProvider.getQuickDocText
     */
    fun listQuickDocText(element: PsiElement): List<String> {
        val gameType = selectGameType(element)
        val result = mutableListOf<String>()
        val eps = ParadoxQuickDocTextProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ep.getQuickDocText(element)?.orNull()?.let { result.add(it) }
        }
        return result
    }
}
