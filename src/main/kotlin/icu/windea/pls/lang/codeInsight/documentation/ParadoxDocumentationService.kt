package icu.windea.pls.lang.codeInsight.documentation

import com.intellij.psi.PsiElement
import icu.windea.pls.base.annotations.ChronicleAnnotationService
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.forEachReversedFast
import icu.windea.pls.core.orNull
import icu.windea.pls.ep.codeInsight.documentation.ParadoxQuickDocTextProvider
import icu.windea.pls.lang.selectGameType

object ParadoxDocumentationService {
    /**
     * @see ParadoxQuickDocTextProvider.getQuickDocText
     */
    @Suppress("unused")
    fun getQuickDocText(element: PsiElement): String? {
        val gameType = selectGameType(element)
        ParadoxQuickDocTextProvider.EP_NAME.extensionList.forEachReversedFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
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
        ParadoxQuickDocTextProvider.EP_NAME.extensionList.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ep.getQuickDocText(element)?.orNull()?.let { result.add(it) }
        }
        return result
    }
}
