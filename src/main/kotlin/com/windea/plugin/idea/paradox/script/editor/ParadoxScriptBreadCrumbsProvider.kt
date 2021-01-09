package com.windea.plugin.idea.paradox.script.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.ui.breadcrumbs.*
import com.windea.plugin.idea.paradox.script.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxScriptBreadCrumbsProvider : BreadcrumbsProvider {
	companion object{
		val defaultLanguages: Array<Language> = arrayOf(ParadoxScriptLanguage)
	}

	override fun getLanguages(): Array<Language> {
		return defaultLanguages
	}

	override fun getElementInfo(element: PsiElement): String {
		return when(element){
			is ParadoxScriptVariable -> element.name
			is ParadoxScriptProperty -> element.name
			is ParadoxScriptBoolean -> element.value
			is ParadoxScriptNumber -> element.value
			is ParadoxScriptString -> element.value
			else -> "<anonymous element>"
		}
	}

	override fun acceptElement(element: PsiElement): Boolean {
		return element is ParadoxScriptVariable
		       || element is ParadoxScriptProperty
		       || element is ParadoxScriptBoolean
		       || element is ParadoxScriptNumber
		       || element is ParadoxScriptString
	}
}
