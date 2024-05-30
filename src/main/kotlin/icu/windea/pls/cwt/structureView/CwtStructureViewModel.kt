package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.util.treeView.smartTree.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.cwt.psi.*

class CwtStructureViewModel(
	editor: Editor?,
	psiFile: PsiFile
) : TextEditorBasedStructureViewModel(editor, psiFile), StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {
	companion object {
		private val defaultSorters = arrayOf(Sorter.ALPHA_SORTER)
	}
	
	//指定根节点，一般为psiFile
	override fun getRoot() = CwtFileTreeElement(psiFile as CwtFile)
	
	//指定在结构视图中的元素
	override fun isSuitable(element: PsiElement?): Boolean {
		return element is CwtFile || element is CwtProperty || (element is CwtValue && element.isBlockValue())
	}
	
	override fun findAcceptableElement(element: PsiElement?): Any? {
		return findAcceptableElementIncludeComment(element) { isSuitable(it) }
	}
	
	//指定可用的排序器，可自定义
	override fun getSorters() = defaultSorters
	
	override fun getCurrentEditorElement(): Any? {
		//		val leafElement = super.getLeafElement(dataContext)
		//		if(leafElement is CwtValue && !leafElement.isLonely()) return leafElement.parentOfType<CwtProperty>()
		//		return leafElement
		return super.getCurrentEditorElement()
	}
	
	override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
		return element is CwtFileTreeElement
	}
	
	override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
		return false
	}
	
	override fun isAutoExpand(element: StructureViewTreeElement): Boolean {
		return element is CwtFileTreeElement
	}
	
	override fun isSmartExpand(): Boolean {
		return false
	}
}

