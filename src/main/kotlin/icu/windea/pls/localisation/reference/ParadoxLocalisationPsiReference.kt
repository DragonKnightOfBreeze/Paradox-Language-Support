package icu.windea.pls.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.ParadoxLocalisationCategory.*

class ParadoxLocalisationPsiReference(
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
		val locale = file.paradoxLocale
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
		val locale = file.paradoxLocale
		val name = element.name
		val project = element.project
		return when(category) {
			Localisation -> findLocalisations(name, locale, project, hasDefault = true)
			SyncedLocalisation -> findSyncedLocalisations(name, locale, project, hasDefault = true)
		}.mapToArray { PsiElementResolveResult(it) }
	}
	
	override fun getVariants(): Array<out Any> {
		val file = element.containingFile as? ParadoxLocalisationFile ?: return emptyArray()
		val category = ParadoxLocalisationCategory.resolve(file) ?: return emptyArray()
		//为了避免这里得到的结果太多，采用关键字查找，这里要去掉作为后缀的dummyIdentifier，并且捕捉异常防止意外
		val keyword = runCatching { element.name.dropLast(dummyIdentifierLength) }.getOrElse { return emptyArray() }
		val project = element.project
		return when(category) {
			Localisation -> findLocalisationsByKeyword(keyword, project)
			SyncedLocalisation -> findSyncedLocalisationsByKeyword(keyword, project)
		}.mapToArray {
			val name = it.name
			val icon = localisationIcon
			//val typeText = it.paradoxFileInfo?.path.toStringOrEmpty()
			val typeText = it.containingFile.name
			LookupElementBuilder.create(it, name).withIcon(icon).withTypeText(typeText, true)
		}
	}
}


