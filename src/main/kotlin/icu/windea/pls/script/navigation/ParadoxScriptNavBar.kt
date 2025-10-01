package icu.windea.pls.script.navigation

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension
import com.intellij.psi.PsiElement
import icu.windea.pls.script.ParadoxScriptLanguage
import javax.swing.Icon

class ParadoxScriptNavBar : StructureAwareNavBarModelExtension() {
    override val language = ParadoxScriptLanguage

    override fun getIcon(o: Any?): Icon? {
        if (o !is PsiElement) return null
        return ParadoxScriptNavigationManager.getIcon(o)
    }

    override fun getPresentableText(o: Any?): String? {
        if (o !is PsiElement) return null
        return ParadoxScriptNavigationManager.getLongPresentableText(o)
    }
}
