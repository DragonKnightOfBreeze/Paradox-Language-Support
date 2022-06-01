package icu.windea.pls.localisation.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.ParadoxLocalisationCategory.*
import icu.windea.pls.localisation.psi.*

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
		ParadoxPredefinedVariableConfig.find(name)?.pointer?.element?.let { return it }
		
		//解析成localisation或者synced_localisation
		return when(category) {
			Localisation -> findLocalisation(name, locale, project, hasDefault = true)
			SyncedLocalisation -> findSyncedLocalisation(name, locale, project, hasDefault = true)
		}
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		val file = element.containingFile as? ParadoxLocalisationFile ?: return emptyArray()
		val category = ParadoxLocalisationCategory.resolve(file) ?: return emptyArray()
		val localeConfig = file.localeConfig
		val name = element.name
		val project = element.project
		
		//尝试解析成predefined_variable
		ParadoxPredefinedVariableConfig.find(name)?.pointer?.element?.let { return arrayOf(PsiElementResolveResult(it)) }
		
		//解析成localisation或者synced_localisation
		return when(category) {
			Localisation -> findLocalisations(name, localeConfig, project, hasDefault = true) //仅查找对应语言区域的
			SyncedLocalisation -> findSyncedLocalisations(name, localeConfig, project, hasDefault = true) //仅查找对应语言区域的
		}.mapToArray {
			PsiElementResolveResult(it)
		}
	}
	
	/**
	 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxPropertyReferenceCompletionProvider
	 */
	@Suppress("RedundantOverride")
	override fun getVariants(): Array<Any> {
		return super<PsiReferenceBase>.getVariants() //not here
	}
}


