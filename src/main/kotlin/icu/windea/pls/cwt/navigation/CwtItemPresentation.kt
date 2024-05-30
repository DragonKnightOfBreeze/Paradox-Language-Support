package icu.windea.pls.cwt.navigation

import com.intellij.ide.util.treeView.*
import com.intellij.navigation.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import javax.swing.*

@Suppress("UNCHECKED_CAST")
class CwtItemPresentation(
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
        if(element is PsiNamedElement) return element.name
        if(element is NavigationItem) return element.name
        return null
    }
    
    //com.intellij.psi.presentation.java.SymbolPresentationUtil.getSymbolContainerText
    
    override fun getLocationString(): String? {
        //使用文件名
        val element = element
        return element?.containingFile?.name
    }
}