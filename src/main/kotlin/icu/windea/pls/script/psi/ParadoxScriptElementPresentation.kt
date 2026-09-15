package icu.windea.pls.script.psi

import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import javax.swing.Icon

class ParadoxScriptElementPresentation(
    val element: PsiElement
) : ItemPresentation {
    override fun getIcon(unused: Boolean): Icon? {
        return ParadoxScriptElementPresentationService.getIcon(element)
    }

    override fun getPresentableText(): String? {
        return ParadoxScriptElementPresentationService.getPresentableText(element)
    }

    override fun getLocationString(): String? {
        return ParadoxScriptElementPresentationService.getLocationString(element)
    }
}
