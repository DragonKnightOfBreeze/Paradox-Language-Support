package icu.windea.pls.cwt.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import icu.windea.pls.core.icon
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement
import icu.windea.pls.lang.psi.CwtConfigElementPresentationService
import javax.swing.Icon

object CwtElementPresentationService {
    fun getIcon(element: PsiElement): Icon? {
        // promote to semantic level if needed
        CwtConfigElementPresentationService.getIcon(element)?.let { return it }
        return element.icon
    }

    fun getPresentableText(element: PsiElement): String? {
        return when (element) {
            // 名字
            is CwtFile -> element.name
            // 名字
            is CwtProperty -> element.name
            // 展示文本
            is PsiPresentableTextAwareElement -> element.presentableText
            // 回退
            is NavigatablePsiElement -> element.name
            else -> null
        }
    }

    fun getLocationString(element: PsiElement): String? {
        return element.containingFile?.name
    }

    fun getPresentableTextInTree(element: PsiElement): String? {
        return getPresentableText(element)
    }

    @Suppress("UNUSED_PARAMETER")
    fun getLocationStringInTree(element: PsiElement): String? {
        return null
    }

    fun getPresentableTextInNavBar(element: PsiElement): String? {
        return getPresentableText(element)
    }

    fun getElementInfoInBreadCrumbs(element: PsiElement): String {
        return getPresentableText(element).orEmpty()
    }
}
