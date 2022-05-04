package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationPropertyListTreeElement(
	element: ParadoxLocalisationPropertyList
) : PsiTreeElementBase<ParadoxLocalisationPropertyList>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val propertyList = element?.propertyList ?: return emptyList()
		if(propertyList.isEmpty()) return emptyList()
		return propertyList.map { ParadoxLocalisationPropertyTreeElement(it) }
	}
	
	override fun getPresentableText(): String? {
		val locale = element?.locale ?: return null
		return locale.name
	}
}