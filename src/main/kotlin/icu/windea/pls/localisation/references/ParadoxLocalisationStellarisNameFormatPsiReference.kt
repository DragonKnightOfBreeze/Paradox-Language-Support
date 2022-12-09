package icu.windea.pls.localisation.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*

@WithGameType(ParadoxGameType.Stellaris)
class ParadoxLocalisationStellarisNameFormatPsiReference(
	element: ParadoxLocalisationStellarisNamePart,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationStellarisNamePart>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		//重命名当前元素
		return element.setName(newElementName)
	}
	
	override fun resolve(): ParadoxValueSetValueElement? {
		val element = element
		val name = element.name ?: return null
		val localisationProperty = element.parentOfType<ParadoxLocalisationProperty>() ?: return null
		val localisationKey = localisationProperty.name
		val project = localisationProperty.project
		val valueSetName =  StellarisNameFormatHandler.getValueSetName(localisationKey, project) ?: return null
		val gameType = ParadoxSelectorUtils.selectGameType(localisationProperty) ?: return null
		val selector = valueSetValueSelector().gameType(gameType).declarationOnly()
		//必须要先有声明
		val declaration = ParadoxValueSetValueSearch.search(name, valueSetName, project, selector = selector).findFirst()
		if(declaration == null) return null
		return ParadoxValueSetValueElement(element, name, valueSetName, project, gameType)
	}
}