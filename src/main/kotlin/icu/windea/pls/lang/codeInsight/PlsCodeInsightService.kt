package icu.windea.pls.lang.codeInsight

import com.intellij.psi.PsiElement
import icu.windea.pls.core.orNull
import icu.windea.pls.ep.codeInsight.hints.ParadoxColorProvider
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProvider
import icu.windea.pls.ep.codeInsight.hints.ParadoxQuickDocTextProvider
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import java.awt.Color

object PlsCodeInsightService {
    fun getColor(element: PsiElement, fromToken: Boolean = false): Color? {
        return ParadoxColorProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            val targetElement = if (fromToken) ep.getTargetElement(element) else element
            if (targetElement == null) return@f null
            ep.getColor(targetElement)
        }
    }

    fun setColor(element: PsiElement, color: Color, fromToken: Boolean = false) {
        ParadoxColorProvider.EP_NAME.extensionList.any f@{ ep ->
            val targetElement = if (fromToken) ep.getTargetElement(element) else element
            if (targetElement == null) return@f false
            ep.setColor(targetElement, color)
        }
    }

    @Suppress("unused")
    fun getHintText(element: PsiElement): String? {
        val gameType = selectGameType(element)
        return ParadoxHintTextProvider.EP_NAME.extensionList.reversed().firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getHintText(element)?.orNull()
        }
    }

    fun getHintLocalisation(element: PsiElement): ParadoxLocalisationProperty? {
        val gameType = selectGameType(element)
        return ParadoxHintTextProvider.EP_NAME.extensionList.reversed().firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getHintLocalisation(element)
        }
    }

    @Suppress("unused")
    fun getQuickDocText(element: PsiElement): String? {
        val gameType = selectGameType(element)
        return ParadoxQuickDocTextProvider.EP_NAME.extensionList.reversed().firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getQuickDocText(element)?.orNull()
        }
    }

    fun listQuickDocText(element: PsiElement): List<String> {
        val gameType = selectGameType(element)
        return ParadoxQuickDocTextProvider.EP_NAME.extensionList.mapNotNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getQuickDocText(element)?.orNull()
        }
    }
}
