package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.psi.PsiElement
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.cwt.navigation.CwtNavigationManager
import javax.swing.Icon

abstract class CwtTreeElement<T : PsiElement>(element: T) : PsiTreeElementBase<T>(element) {
    override fun getIcon(open: Boolean): Icon? {
        val element = element ?: return null
        return CwtNavigationManager.getIcon(element)
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        return CwtNavigationManager.getPresentableText(element)
    }

    override fun getLocationString(): String? {
        val element = element ?: return null
        return CwtNavigationManager.getLocationString(element)
    }

    protected fun PsiElement.toTreeElement(): CwtTreeElement<out PsiElement>? {
        return when (this) {
            is CwtValue -> CwtValueTreeElement(this)
            is CwtProperty -> CwtPropertyTreeElement(this)
            else -> null
        }
    }
}
