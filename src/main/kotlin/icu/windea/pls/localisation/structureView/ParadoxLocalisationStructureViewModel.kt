package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.util.treeView.smartTree.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationStructureViewModel(
	editor: Editor?,
	file: PsiFile
) : TextEditorBasedStructureViewModel(editor, file), StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {
	companion object {
		private val defaultSorters = arrayOf(Sorter.ALPHA_SORTER)
	}
	
	//指定根节点，一般为psiFile
	override fun getRoot() = ParadoxLocalisationFileTreeElement(psiFile as ParadoxLocalisationFile)
	
	//指定在结构视图中的元素
	override fun isSuitable(element: PsiElement?): Boolean {
		return element is ParadoxLocalisationFile || element is ParadoxLocalisationPropertyList || element is ParadoxLocalisationProperty
	}
	
	override fun findAcceptableElement(element: PsiElement?): Any? {
		var current = element
		while(current != null && current !is PsiFile) {
			if(isSuitable(current)) return current
			if(current is PsiComment) return current.siblings().find { it is CwtProperty }
				?.takeIf { it.prevSibling.isSpaceOrSingleLineBreak() }
			current = current.parent
		}
		return null
	}
	
	//指定可用的排序器，可自定义
	override fun getSorters() = defaultSorters
	
	override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
		return element is ParadoxLocalisationFileTreeElement || element is ParadoxLocalisationPropertyListTreeElement
	}
	
	override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
		return false
	}
	
	override fun isAutoExpand(element: StructureViewTreeElement): Boolean {
		return element is ParadoxLocalisationFileTreeElement || element is ParadoxLocalisationPropertyListTreeElement
	}
	
	override fun isSmartExpand(): Boolean {
		return false
	}
}
