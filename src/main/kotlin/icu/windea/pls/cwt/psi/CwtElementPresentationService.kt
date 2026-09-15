package icu.windea.pls.cwt.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import icu.windea.pls.core.icon
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement
import icu.windea.pls.lang.psi.CwtConfigElementPresentationService
import javax.swing.Icon

object CwtElementPresentationService {
    fun getIcon(element: PsiElement): Icon? {
        // 3.0.3 promote to semantic level if needed
        CwtConfigElementPresentationService.getIcon(element)?.let { return it }

        return element.icon
    }

    fun getPresentableText(element: PsiElement): String? {
        return when (element) {
            is CwtFile -> element.name
            is CwtOption -> element.name
            is CwtProperty -> element.name
            is PsiPresentableTextAwareElement -> element.presentableText
            is NavigatablePsiElement -> element.name
            else -> null
        }
    }

    fun getLocationString(element: PsiElement): String? {
        return element.containingFile?.name
    }

    @Suppress("UNUSED_PARAMETER")
    fun getTreeLocationString(element: PsiElement): String? {
        return null
    }

    fun getPresentableTextInNavBar(element: PsiElement): String? {
        return getPresentableText(element)
    }

    fun getElementInfoInBreadCrumbs(element: PsiElement): String {
        return getPresentableText(element).orEmpty()
    }
}
