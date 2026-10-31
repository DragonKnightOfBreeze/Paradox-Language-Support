package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.icon
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.lang.psi.CwtConfigElementPresentationService
import javax.swing.Icon

object CwtElementPresentationService {
    fun getIcon(element: PsiElement): Icon? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        // 3.0.3 promote to semantic level if needed
        CwtConfigElementPresentationService.getIcon(element)?.let { return it }

        return element.icon
    }

    fun getPresentableText(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        return when (element) {
            is CwtOption -> element.name
            is CwtProperty -> element.name
            else -> PsiService.getDefaultPresentableText(element)
        }
    }

    fun getLocationString(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        return element.containingFile?.name
    }

    @Suppress("UNUSED_PARAMETER")
    fun getTreeLocationString(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        return null
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

    private fun checkLanguage(element: PsiElement): Boolean = element.language == CwtLanguage
}
