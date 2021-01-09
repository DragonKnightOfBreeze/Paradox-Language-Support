package com.windea.plugin.idea.paradox.localisation.structureView

import com.intellij.ide.structureView.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*

class ParadoxLocalisationStructureViewFactory : PsiStructureViewFactory {
	override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder {
		return object : TreeBasedStructureViewBuilder() {
			override fun createStructureViewModel(editor: Editor?): StructureViewModel {
				return ParadoxLocalisationStructureViewModel(editor, psiFile)
			}
		}
	}
}

