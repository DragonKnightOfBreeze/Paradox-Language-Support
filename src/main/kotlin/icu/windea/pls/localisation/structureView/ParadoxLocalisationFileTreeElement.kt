package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.localisation.psi.*

@Suppress("HasPlatformType", "UNCHECKED_CAST")
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

