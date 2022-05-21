package icu.windea.pls.cwt.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.ui.breadcrumbs.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*

class CwtBreadCrumbsProvider : BreadcrumbsProvider {
	companion object {
		private val defaultLanguages: Array<Language> = arrayOf(CwtLanguage)
	}
	
	override fun getLanguages(): Array<Language> {
		return defaultLanguages
	}
	
	override fun acceptElement(element: PsiElement): Boolean {
		return element is CwtProperty || (element is CwtValue && element.parent is CwtBlock)
	}
	
	override fun getElementInfo(element: PsiElement): String {
		return when {
			element is CwtProperty -> element.name
			element is CwtValue -> element.value
			else -> throw InternalError()
		}
	}
}