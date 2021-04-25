package com.windea.plugin.idea.pls.cwt.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.intellij.ide.util.treeView.smartTree.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.cwt.psi.*
import com.windea.plugin.idea.pls.script.psi.*
import com.windea.plugin.idea.pls.script.structureView.*

class CwtStructureViewModel(
	editor: Editor?,
	psiFile: PsiFile
) : TextEditorBasedStructureViewModel(editor, psiFile), StructureViewModel.ElementInfoProvider {
	companion object {
		private val defaultSuitableClasses = arrayOf(
			CwtFile::class.java,
			CwtProperty::class.java,
			CwtValue::class.java
		)
		private val defaultSorters = arrayOf(Sorter.ALPHA_SORTER)
	}
	
	//指定根节点，一般为psiFile
	override fun getRoot() = CwtFileTreeElement(psiFile as CwtFile)
	
	//指定在结构视图中的元素
	override fun getSuitableClasses() = defaultSuitableClasses
	
	//指定可用的排序器，可自定义
	override fun getSorters() = defaultSorters
	
	override fun isAlwaysShowsPlus(element: StructureViewTreeElement) = element.value is CwtFile
	
	override fun isAlwaysLeaf(element: StructureViewTreeElement) = false
}

