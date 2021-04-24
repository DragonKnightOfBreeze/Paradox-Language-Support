package com.windea.plugin.idea.pls.localisation.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.windea.plugin.idea.pls.localisation.psi.*

class ParadoxLocalisationPropertyTreeElement(
	private val element: ParadoxLocalisationProperty
) : PsiTreeElementBase<ParadoxLocalisationProperty>(element) {
	override fun getChildrenBase(): MutableCollection<StructureViewTreeElement> {
		return mutableListOf()
	}

	override fun getPresentableText(): String {
		return element.name
	}
}

