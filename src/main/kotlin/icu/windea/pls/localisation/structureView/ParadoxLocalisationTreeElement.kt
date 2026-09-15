package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.psi.PsiElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementPresentationService
import javax.swing.Icon

abstract class ParadoxLocalisationTreeElement<T : PsiElement>(element: T) : PsiTreeElementBase<T>(element) {
    override fun getIcon(open: Boolean): Icon? {
        val element = element ?: return null
        return ParadoxLocalisationElementPresentationService.getIcon(element)
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        return ParadoxLocalisationElementPresentationService.getPresentableTextInTree(element)
    }

    override fun getLocationString(): String? {
        val element = element ?: return null
        return ParadoxLocalisationElementPresentationService.getLocationStringInTree(element)
    }

    override fun isSearchInLocationString(): Boolean {
        return true
    }

    protected fun PsiElement.toTreeElement(): ParadoxLocalisationTreeElement<out PsiElement>? {
        return when (this) {
            is ParadoxLocalisationPropertyList -> ParadoxLocalisationPropertyListTreeElement(this)
            is ParadoxLocalisationProperty -> ParadoxLocalisationPropertyTreeElement(this)
            else -> null
        }
    }
}
