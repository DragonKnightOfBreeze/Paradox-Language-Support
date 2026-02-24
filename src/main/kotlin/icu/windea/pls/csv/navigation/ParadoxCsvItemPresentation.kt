package icu.windea.pls.csv.navigation

import com.intellij.ide.util.treeView.TreeAnchorizer
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import javax.swing.Icon

class ParadoxCsvItemPresentation(
    element: PsiElement
) : ItemPresentation {
    private val anchor = TreeAnchorizer.getService().createAnchor(element)
    private val element get() = TreeAnchorizer.getService().retrieveElement(anchor) as PsiElement?

    override fun getIcon(unused: Boolean): Icon? {
        val element = element ?: return null
        return ParadoxCsvNavigationManager.getIcon(element)
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        return ParadoxCsvNavigationManager.getPresentableText(element)
    }

    override fun getLocationString(): String? {
        val element = element ?: return null
        return ParadoxCsvNavigationManager.getLocationString(element)
    }
}
