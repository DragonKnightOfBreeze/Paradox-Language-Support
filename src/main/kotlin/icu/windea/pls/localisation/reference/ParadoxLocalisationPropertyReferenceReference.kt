package icu.windea.pls.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.core.ParadoxLocalisationCategory.*

class ParadoxLocalisationPropertyReferenceReference(
	element: ParadoxLocalisationPropertyReference,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationPropertyReference>(element, rangeInElement), PsiPolyVariantReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//TODO 重命名关联的definition
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		val file = element.containingFile as? ParadoxLocalisationFile ?: return null
		val category = ParadoxLocalisationCategory.resolve(file) ?: return null
		val locale = file.localeConfig
		val name = element.name
		val project = element.project
		return when(category) {
			Localisation -> findLocalisation(name, locale, project, hasDefault = true)
			SyncedLocalisation -> findSyncedLocalisation(name, locale, project, hasDefault = true)
		}
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		val file = element.containingFile as? ParadoxLocalisationFile ?: return emptyArray()
		val category = ParadoxLocalisationCategory.resolve(file) ?: return emptyArray()
		val locale = file.localeConfig
		val name = element.name
		val project = element.project
		return when(category) {
			Localisation -> findLocalisations(name, locale, project, hasDefault = true)
			SyncedLocalisation -> findSyncedLocalisations(name, locale, project, hasDefault = true)
		}.mapToArray { 
			PsiElementResolveResult(it)
		}
	}
	
	override fun getVariants(): Array<out Any> {
		val file = element.containingFile as? ParadoxLocalisationFile ?: return emptyArray()
		val category = ParadoxLocalisationCategory.resolve(file) ?: return emptyArray()
		//为了避免这里得到的结果太多，采用关键字查找
		val keyword = element.keyword
		val project = element.project
		return when(category) {
			Localisation -> findLocalisationsByKeyword(keyword, project)
			SyncedLocalisation -> findSyncedLocalisationsByKeyword(keyword, project)
		}.mapToArray {
			val name = it.name
			val icon = it.icon
			val typeText = it.containingFile.name
			LookupElementBuilder.create(it, name).withIcon(icon).withTypeText(typeText, true)
		}
	}
}


