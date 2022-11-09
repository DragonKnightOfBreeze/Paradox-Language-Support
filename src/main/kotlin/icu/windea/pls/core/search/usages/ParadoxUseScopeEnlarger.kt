package icu.windea.pls.core.search.usages

import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*

/**
 * 为了从项目文件中的声明导航到库中的引用。
 */
class ParadoxUseScopeEnlarger : UseScopeEnlarger() {
	override fun getAdditionalUseScope(element: PsiElement): SearchScope? {
		if(element is PsiFile) {
			if(element.fileInfo != null) return GlobalSearchScope.allScope(element.project)
		}
		val language = element.language
		return when(language) {
			CwtLanguage, ParadoxScriptLanguage, ParadoxLocalisationLanguage -> GlobalSearchScope.allScope(element.project)
			else -> null
		}
	}
}