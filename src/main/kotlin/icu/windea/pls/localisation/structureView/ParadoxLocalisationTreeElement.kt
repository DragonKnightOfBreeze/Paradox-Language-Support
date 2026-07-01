package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.psi.PsiElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiPresentationService
import javax.swing.Icon

abstract class ParadoxLocalisationTreeElement<T : PsiElement>(element: T) : PsiTreeElementBase<T>(element) {
    override fun getIcon(open: Boolean): Icon? {
        val element = element ?: return null
        return ParadoxLocalisationPsiPresentationService.getIcon(element)
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        return ParadoxLocalisationPsiPresentationService.getTreePresentableText(element)
    }

    override fun getLocationString(): String? {
        val element = element ?: return null
        return ParadoxLocalisationPsiPresentationService.getTreeLocationString(element)
    }

    protected fun PsiElement.toTreeElement(): ParadoxLocalisationTreeElement<out PsiElement>? {
        return when (this) {
            is ParadoxLocalisationPropertyList -> ParadoxLocalisationPropertyListTreeElement(this)
            is ParadoxLocalisationProperty -> ParadoxLocalisationPropertyTreeElement(this)
            else -> null
        }
    }
}
