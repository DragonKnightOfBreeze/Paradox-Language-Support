package icu.windea.pls.lang

import com.intellij.ide.IconProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.script.navigation.ParadoxScriptNavigationManager
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import javax.swing.Icon

/**
 * 为一些特定的 PSI 元素提供特殊图标。
 */
class ParadoxIconProvider : IconProvider() {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        return when (element) {
            is ParadoxScriptFile -> ParadoxScriptNavigationManager.getPatchedIcon(element)
            is ParadoxScriptProperty -> ParadoxScriptNavigationManager.getPatchedIcon(element)
            else -> null
        }
    }
}
