package icu.windea.pls.localisation.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.ui.breadcrumbs.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationBreadCrumbsProvider : BreadcrumbsProvider {
	companion object {
		private val defaultLanguages: Array<Language> = arrayOf(ParadoxLocalisationLanguage)
	}
	
	override fun getLanguages(): Array<Language> {
		return defaultLanguages
	}
	
	override fun acceptElement(element: PsiElement): Boolean {
		return element is ParadoxLocalisationLocale || element is ParadoxLocalisationPropertyList || element is ParadoxLocalisationProperty
	}
	
	override fun getElementInfo(element: PsiElement): String {
		return when(element) {
			is ParadoxLocalisationLocale -> element.name
			is ParadoxLocalisationPropertyList -> element.locale.name
			is ParadoxLocalisationProperty -> element.name
			else -> throw InternalError()
		}
	}
}
