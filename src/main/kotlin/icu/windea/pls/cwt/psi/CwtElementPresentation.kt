package icu.windea.pls.cwt.psi

import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import javax.swing.Icon

class CwtElementPresentation(
    val element: PsiElement
) : ItemPresentation {
    override fun getIcon(unused: Boolean): Icon? {
        return CwtElementPresentationService.getIcon(element)
    }

    override fun getPresentableText(): String? {
        return CwtElementPresentationService.getPresentableText(element)
    }

    override fun getLocationString(): String? {
        return CwtElementPresentationService.getLocationString(element)
    }
}
