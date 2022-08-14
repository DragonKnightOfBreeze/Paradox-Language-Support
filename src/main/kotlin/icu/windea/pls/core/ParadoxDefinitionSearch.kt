package icu.windea.pls.core

import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*

class ParadoxDefinitionSearch: QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
	override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
		return true
	}
}