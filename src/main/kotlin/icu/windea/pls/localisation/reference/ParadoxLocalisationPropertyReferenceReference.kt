package icu.windea.pls.localisation.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.ParadoxLocalisationCategory.*
import icu.windea.pls.core.selector.*

class ParadoxLocalisationPropertyReferenceReference(
	element: ParadoxLocalisationPropertyReference,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationPropertyReference>(element, rangeInElement), PsiPolyVariantReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//TODO 重命名关联的definition
		return element.setName(newElementName)
	}
	
	//TODO may be resolved to localisation / variable / system statistics in GUI elements 
	
	override fun resolve(): PsiElement? {
		val file = element.containingFile as? ParadoxLocalisationFile ?: return null
		val category = ParadoxLocalisationCategory.resolve(file) ?: return null
		val locale = file.localeConfig
		val name = element.name
		val project = element.project
		
		//尝试解析成predefined_variable
		InternalConfigHandler.getPredefinedVariable(name)?.pointer?.element?.let { return it }
		
		//解析成localisation或者synced_localisation
		val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(locale)
		return when(category) {
			Localisation -> findLocalisation(name, project, selector = selector)
			SyncedLocalisation -> findSyncedLocalisation(name, project, selector = selector)
		}
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		val file = element.containingFile as? ParadoxLocalisationFile ?: return emptyArray()
		val category = ParadoxLocalisationCategory.resolve(file) ?: return emptyArray()
		val locale = file.localeConfig
		val name = element.name
		val project = element.project
		
		//尝试解析成predefined_variable
		InternalConfigHandler.getPredefinedVariable(name)?.pointer?.element?.let { return arrayOf(PsiElementResolveResult(it)) }
		
		//解析成localisation或者synced_localisation
		val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file) //不指定偏好的语言区域
		return when(category) {
			Localisation -> findLocalisations(name, project, selector = selector) //仅查找对应语言区域的
			SyncedLocalisation -> findSyncedLocalisations(name, project, selector = selector) //仅查找对应语言区域的
		}.mapToArray {
			PsiElementResolveResult(it)
		}
	}
	
	/**
	 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxPropertyReferenceCompletionProvider
	 */
	override fun getVariants(): Array<Any> {
		return super<PsiReferenceBase>.getVariants() //not here
	}
}


