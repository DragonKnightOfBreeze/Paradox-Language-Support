package icu.windea.pls.csv.navigation

import com.intellij.ide.util.treeView.TreeAnchorizer
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import icu.windea.pls.core.icon
import icu.windea.pls.lang.fileInfo
import javax.swing.Icon

class ParadoxCsvItemPresentation(
    element: PsiElement
) : ItemPresentation {
    private val anchor = TreeAnchorizer.getService().createAnchor(element)

    val element get() = TreeAnchorizer.getService().retrieveElement(anchor) as PsiElement?

    override fun getIcon(unused: Boolean): Icon? {
        val element = element
        return element?.icon
    }

    //com.intellij.psi.presentation.java.SymbolPresentationUtil.getSymbolPresentableText

    override fun getPresentableText(): String? {
        //使用PSI元素的名字
        val element = element
        if (element is PsiNamedElement) return element.name
        if (element is NavigationItem) return element.name
        return null
    }

    //com.intellij.psi.presentation.java.SymbolPresentationUtil.getSymbolContainerText

    override fun getLocationString(): String? {
        //使用相对于游戏或模组目录的路径，或者使用虚拟文件的绝对路径
        val element = element
        if (element == null) return null
        return element.fileInfo?.path?.path
            ?: element.containingFile?.virtualFile?.path
    }
}
