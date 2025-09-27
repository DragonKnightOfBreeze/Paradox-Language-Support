package icu.windea.pls.csv.navigation

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension
import com.intellij.psi.PsiElement
import icu.windea.pls.csv.ParadoxCsvLanguage
import javax.swing.Icon

class ParadoxCsvNavBar : StructureAwareNavBarModelExtension() {
    override val language = ParadoxCsvLanguage

    override fun getIcon(o: Any?): Icon? {
        if (o !is PsiElement) return null
        return ParadoxCsvNavigationManager.getIcon(o)
    }

    override fun getPresentableText(o: Any?): String? {
        if (o !is PsiElement) return null
        return ParadoxCsvNavigationManager.getLongPresentableText(o)
    }
}
