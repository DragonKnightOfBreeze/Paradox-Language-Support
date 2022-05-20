package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationPropertyListTreeElement(
	element: ParadoxLocalisationPropertyList
) : PsiTreeElementBase<ParadoxLocalisationPropertyList>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		return element?.mapChildOfType(ParadoxLocalisationProperty::class.java) {
			ParadoxLocalisationPropertyTreeElement(it)
		} ?: emptyList()
	}
	
	override fun getPresentableText(): String? {
		val locale = element?.locale ?: return null
		return locale.name
	}
	
	override fun getLocationString(): String? {
		val localeConfig = element?.localeConfig ?: return null
		return localeConfig.description
	}
}