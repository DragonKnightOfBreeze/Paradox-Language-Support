package icu.windea.pls.localisation.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.model.ParadoxLocalisationCategory.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.localisation.psi.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationPropertyReferenceCompletionProvider
 */
class ParadoxLocalisationPropertyPsiReference(
	element: ParadoxLocalisationPropertyReference,
	rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxLocalisationPropertyReference>(element, rangeInElement), SmartPsiReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//TODO 重命名关联的definition
		return element.setName(newElementName)
	}
	
	//TODO may be resolved to localisation / parameter / system statistics in GUI elements 
	
	override fun resolve(): PsiElement? {
		return resolve(true)
	}
	
	override fun resolve(exact: Boolean): PsiElement? {
		val file = element.containingFile as? ParadoxLocalisationFile ?: return null
		val category = ParadoxLocalisationCategory.resolve(file) ?: return null
		val locale = file.localeConfig
		val name = element.name
		val project = element.project
		
		//尝试解析成predefined_parameter
		getCwtConfig(project).core.localisationLocales.get(name)?.pointer?.element?.let { return it }
		
		//解析成localisation或者synced_localisation
		val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(locale)
		return when(category) {
			Localisation -> ParadoxLocalisationSearch.search(name, project, selector = selector).find()
			SyncedLocalisation -> ParadoxSyncedLocalisationSearch.search(name, project, selector = selector).find()
		}
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		val file = element.containingFile as? ParadoxLocalisationFile ?: return emptyArray()
		val category = ParadoxLocalisationCategory.resolve(file) ?: return emptyArray()
		val locale = file.localeConfig
		val name = element.name
		val project = element.project
		
		//尝试解析成predefined_parameter
		getCwtConfig(project).core.localisationLocales.get(name)?.pointer?.element?.let { return arrayOf(PsiElementResolveResult(it)) }
		
		//解析成localisation或者synced_localisation
		val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(locale)
		return when(category) {
			Localisation -> ParadoxLocalisationSearch.search(name, project, selector = selector).findAll() //仅查找对应语言区域的
			SyncedLocalisation -> ParadoxSyncedLocalisationSearch.search(name, project, selector = selector).findAll() //仅查找对应语言区域的
		}.mapToArray { PsiElementResolveResult(it) }
	}
}


