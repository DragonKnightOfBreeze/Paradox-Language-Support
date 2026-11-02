package icu.windea.pls.localisation.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.icon
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.lang.psi.ParadoxElementPresentationService
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import javax.swing.Icon

object ParadoxLocalisationElementPresentationService {
    fun getIcon(element: PsiElement): Icon? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        // 3.0.3 promote to semantic level if needed
        ParadoxElementPresentationService.getIcon(element)?.let { return it }

        return element.icon
    }

    fun getPresentableText(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        // 3.0.3 promote to semantic level if needed
        ParadoxElementPresentationService.getPresentableText(element)?.let { return it }

        return when (element) {
            is ParadoxLocalisationLocale -> element.name
            is ParadoxLocalisationPropertyList -> element.locale?.name.orAnonymous()
            is ParadoxLocalisationProperty -> element.name
            else -> PsiService.getDefaultPresentableText(element)
        }
    }

    fun getLocationString(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        // 3.0.3 promote to semantic level if needed
        ParadoxElementPresentationService.getLocationString(element)?.let { return it }

        return element.containingFile?.name
    }

    fun getTreeLocationString(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        // 3.0.3 promote to semantic level if needed
        ParadoxElementPresentationService.getTreeLocationString(element)?.let { return it }

        return when (element) {
            is ParadoxLocalisationPropertyList -> selectLocale(element)?.text
            else -> null
        }
    }

    fun getPresentableTextInNavBar(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        return getPresentableText(element)
    }

    fun getElementInfoInBreadCrumbs(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        return getPresentableText(element)
    }

    private fun checkLanguage(element: PsiElement): Boolean = element.language == ParadoxLocalisationLanguage
}
