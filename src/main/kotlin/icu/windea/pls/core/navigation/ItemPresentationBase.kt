package icu.windea.pls.core.navigation

import com.intellij.ide.util.treeView.*
import com.intellij.navigation.*
import com.intellij.psi.*
import icu.windea.pls.*
import javax.swing.*

@Suppress("UNCHECKED_CAST")
abstract class ItemPresentationBase<T : PsiElement>(
	element: T
) : ItemPresentation, LocationPresentation {
	private val anchor = TreeAnchorizer.getService().createAnchor(element)
	
	val element get() = TreeAnchorizer.getService().retrieveElement(anchor) as T?
	
	override fun getIcon(unused: Boolean): Icon? {
		val element = element
		return element?.icon
	}
	
	//com.intellij.psi.presentation.java.SymbolPresentationUtil.getSymbolPresentableText
	
	override fun getPresentableText(): String? {
		//默认使用PSI元素的名字
		val element = element
		if(element is PsiNamedElement) return element.name
		return null
	}
	
	//com.intellij.psi.presentation.java.SymbolPresentationUtil.getSymbolContainerText
	
	override fun getLocationString(): String? {
		//默认使用虚拟文件的路径
		val element = element
		return element?.containingFile?.virtualFile?.path
	}
	
	override fun getLocationPrefix(): String {
		return " ["
	}
	
	override fun getLocationSuffix(): String {
		return "]"
	}
}

