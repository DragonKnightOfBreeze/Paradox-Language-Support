package icu.windea.pls.script.psi

import com.intellij.ide.util.treeView.TreeAnchorizer
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import javax.swing.Icon

class ParadoxScriptPsiPresentation(
    element: PsiElement
) : ItemPresentation {
    private val anchor = TreeAnchorizer.getService().createAnchor(element)
    private val element get() = TreeAnchorizer.getService().retrieveElement(anchor) as PsiElement?

    override fun getIcon(unused: Boolean): Icon? {
        val element = element ?: return null
        return ParadoxScriptPsiPresentationService.getIcon(element)
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        return ParadoxScriptPsiPresentationService.getPresentableText(element)
    }

    override fun getLocationString(): String? {
        val element = element ?: return null
        return ParadoxScriptPsiPresentationService.getLocationString(element)
    }
}
