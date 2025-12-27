package icu.windea.pls.lang

import com.intellij.ide.IconProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.cwt.navigation.CwtNavigationManager
import icu.windea.pls.cwt.psi.CwtMember
import javax.swing.Icon

/**
 * 为规则文件中的一些特定的 PSI 元素提供特殊图标。
 */
class CwtConfigIconProvider : IconProvider() {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        return when (element) {
            is CwtMember -> CwtNavigationManager.getPatchedIcon(element)
            else -> null
        }
    }
}
