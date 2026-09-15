package icu.windea.pls.csv.psi

import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import javax.swing.Icon

class ParadoxCsvElementPresentation(
    val element: PsiElement
) : ItemPresentation {
    override fun getIcon(unused: Boolean): Icon? {
        return ParadoxCsvElementPresentationService.getIcon(element)
    }

    override fun getPresentableText(): String? {
        return ParadoxCsvElementPresentationService.getPresentableText(element)
    }

    override fun getLocationString(): String? {
        return ParadoxCsvElementPresentationService.getLocationString(element)
    }
}
