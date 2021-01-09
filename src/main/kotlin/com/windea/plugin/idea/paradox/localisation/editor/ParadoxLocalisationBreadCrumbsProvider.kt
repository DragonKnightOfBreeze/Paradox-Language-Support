package com.windea.plugin.idea.paradox.localisation.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.ui.breadcrumbs.*
import com.windea.plugin.idea.paradox.localisation.*
import com.windea.plugin.idea.paradox.localisation.psi.*

class ParadoxLocalisationBreadCrumbsProvider : BreadcrumbsProvider {
	companion object{
		val defaultLanguages:Array<Language> = arrayOf(ParadoxLocalisationLanguage)
	}

	override fun getLanguages(): Array<Language> {
		return defaultLanguages
	}

	override fun getElementInfo(element: PsiElement): String {
		return when(element){
			is ParadoxLocalisationProperty -> element.name
			else -> "<anonymous element>"
		}
	}

	override fun acceptElement(element: PsiElement): Boolean {
		return element is ParadoxLocalisationProperty
	}
}
