package icu.windea.pls.core.search.usages

import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*

fun getUseScope(queryParameters: ReferencesSearch.SearchParameters): SearchScope {
	//use allScope to allow search usages in libraries while element to search is in project
	return GlobalSearchScope.allScope(queryParameters.project)
	//return queryParameters.elementToSearch.useScope
}