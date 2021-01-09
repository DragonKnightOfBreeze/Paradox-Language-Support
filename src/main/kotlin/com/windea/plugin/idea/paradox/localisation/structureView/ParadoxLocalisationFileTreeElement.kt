@file:Suppress("HasPlatformType", "UNCHECKED_CAST")

package com.windea.plugin.idea.paradox.localisation.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.windea.plugin.idea.paradox.localisation.psi.*

class ParadoxLocalisationFileTreeElement(
	private val element: ParadoxLocalisationFile
) : PsiTreeElementBase<ParadoxLocalisationFile>(element) {
	override fun getChildrenBase(): MutableCollection<StructureViewTreeElement> {
		return element.properties.mapTo(mutableListOf()) { ParadoxLocalisationPropertyTreeElement(it) }
	}

	override fun getPresentableText(): String {
		return element.name
	}
}

