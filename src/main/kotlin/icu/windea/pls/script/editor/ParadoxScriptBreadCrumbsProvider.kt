package icu.windea.pls.script.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.ui.breadcrumbs.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

class ParadoxScriptBreadCrumbsProvider : BreadcrumbsProvider {
	companion object {
		private val defaultLanguages: Array<Language> = arrayOf(ParadoxScriptLanguage)
	}
	
	override fun getLanguages(): Array<Language> {
		return defaultLanguages
	}
	
	override fun acceptElement(element: PsiElement): Boolean {
		return element is ParadoxScriptProperty || (element is ParadoxScriptValue && !element.isPropertyValue())
			|| element is ParadoxScriptScriptedVariable
	}
	
	override fun getElementInfo(element: PsiElement): String {
		return when(element) {
			is ParadoxScriptProperty -> element.name
			is ParadoxScriptValue -> element.value
			is ParadoxScriptScriptedVariable -> element.name
			else -> throw InternalError()
		}
	}
}
