package icu.windea.pls.localisation.psi

import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import javax.swing.Icon

class ParadoxLocalisationElementPresentation(
    val element: PsiElement
) : ItemPresentation {
    override fun getIcon(unused: Boolean): Icon? {
        return ParadoxLocalisationElementPresentationService.getIcon(element)
    }

    override fun getPresentableText(): String? {
        return ParadoxLocalisationElementPresentationService.getPresentableText(element)
    }

    override fun getLocationString(): String? {
        return ParadoxLocalisationElementPresentationService.getLocationString(element)
    }
}
