package icu.windea.pls.lang.model

import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.script.psi.*

class ParadoxSearchScope(private val searchScope: String) {
	fun findRoot(context: PsiElement): PsiElement? {
		return when {
			searchScope == "definition" -> context.findParentDefinition()
			else -> null
		}
	}
	
	fun getGlobalSearchScope(context: PsiElement): GlobalSearchScope? {
		return when {
			searchScope == "definition" -> GlobalSearchScope.fileScope(context.containingFile) //TODO
			else -> null
		}
	}
}