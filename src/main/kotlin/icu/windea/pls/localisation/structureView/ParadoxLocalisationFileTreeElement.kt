package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationFileTreeElement(
	private val element: ParadoxLocalisationFile
) : PsiTreeElementBase<ParadoxLocalisationFile>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		return element.properties.map { ParadoxLocalisationPropertyTreeElement(it) }
	}
	
	override fun getPresentableText(): String {
		return element.name
	}
}

