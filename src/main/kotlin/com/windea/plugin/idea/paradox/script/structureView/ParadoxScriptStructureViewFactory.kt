package com.windea.plugin.idea.paradox.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*

class ParadoxScriptStructureViewFactory : PsiStructureViewFactory {
	override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder {
		return object : TreeBasedStructureViewBuilder() {
			override fun createStructureViewModel(editor: Editor?): StructureViewModel {
				return ParadoxScriptStructureViewModel(editor, psiFile)
			}
		}
	}
}
