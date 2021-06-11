package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationPropertyTreeElement(
	private val element: ParadoxLocalisationProperty
) : PsiTreeElementBase<ParadoxLocalisationProperty>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		return emptyList()
	}

	override fun getPresentableText(): String {
		return element.name
	}
}

