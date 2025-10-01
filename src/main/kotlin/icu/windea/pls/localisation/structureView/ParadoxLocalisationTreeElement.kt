package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.psi.PsiElement
import icu.windea.pls.localisation.navigation.ParadoxLocalisationNavigationManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import javax.swing.Icon

abstract class ParadoxLocalisationTreeElement<T : PsiElement>(element: T) : PsiTreeElementBase<T>(element) {
    override fun getIcon(open: Boolean): Icon? {
        val element = element ?: return null
        return ParadoxLocalisationNavigationManager.getIcon(element)
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        return ParadoxLocalisationNavigationManager.getPresentableText(element)
    }

    override fun getLocationString(): String? {
        val element = element ?: return null
        return ParadoxLocalisationNavigationManager.getLocationString(element)
    }

    protected fun PsiElement.toTreeElement(): ParadoxLocalisationTreeElement<out PsiElement>? {
        return when (this) {
            is ParadoxLocalisationPropertyList -> ParadoxLocalisationPropertyListTreeElement(this)
            is ParadoxLocalisationProperty -> ParadoxLocalisationPropertyTreeElement(this)
            else -> null
        }
    }
}
