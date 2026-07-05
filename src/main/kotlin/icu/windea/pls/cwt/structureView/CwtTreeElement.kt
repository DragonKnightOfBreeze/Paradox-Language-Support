package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.psi.PsiElement
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtPsiPresentationService
import icu.windea.pls.cwt.psi.CwtValue
import javax.swing.Icon

abstract class CwtTreeElement<T : PsiElement>(element: T) : PsiTreeElementBase<T>(element) {
    override fun getPresentableText(): String? {
        val element = element ?: return null
        return CwtPsiPresentationService.getTreePresentableText(element)
    }

    override fun getLocationString(): String? {
        val element = element ?: return null
        return CwtPsiPresentationService.getTreeLocationString(element)
    }

    override fun getIcon(open: Boolean): Icon? {
        val element = element ?: return null
        return CwtPsiPresentationService.getIcon(element)
    }

    protected fun PsiElement.toTreeElement(): CwtTreeElement<out PsiElement>? {
        return when (this) {
            is CwtValue -> CwtValueTreeElement(this)
            is CwtProperty -> CwtPropertyTreeElement(this)
            else -> null
        }
    }
}
