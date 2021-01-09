package com.windea.plugin.idea.paradox.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.util.treeView.smartTree.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxScriptStructureViewModel(
	editor: Editor?,
	psiFile: PsiFile
) : TextEditorBasedStructureViewModel(editor, psiFile), StructureViewModel.ElementInfoProvider {
	companion object {
		private val defaultSuitableClasses = arrayOf(
			ParadoxScriptFile::class.java,
			ParadoxScriptVariable::class.java,
			ParadoxScriptProperty::class.java,
			ParadoxScriptPropertyValue::class.java,
			ParadoxScriptValue::class.java
		)
		private val defaultSorters = arrayOf(Sorter.ALPHA_SORTER)
	}
	
	//指定根节点，一般为psiFile
	override fun getRoot() = ParadoxScriptFileTreeElement(psiFile as ParadoxScriptFile)

	//指定在结构视图中的元素
	override fun getSuitableClasses() = defaultSuitableClasses

	//指定可用的排序器，可自定义
	override fun getSorters() = defaultSorters

	override fun isAlwaysShowsPlus(element: StructureViewTreeElement) = element.value is ParadoxScriptFile

	override fun isAlwaysLeaf(element: StructureViewTreeElement) = false
}
