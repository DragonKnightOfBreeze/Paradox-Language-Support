package icu.windea.pls.core.search.implementations

import com.intellij.openapi.application.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*

/**
 * 本地化的实现的查询。加入所有作用域内的包括不同语言区域在内的同名本地化。
 */
class ParadoxLocalisationImplementationsSearch : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
	override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
		//得到解析后的PSI元素
		runReadAction {
			val sourceElement = queryParameters.element
			if(sourceElement is ParadoxLocalisationProperty) {
				val localisationInfo = sourceElement.localisationInfo
				if(localisationInfo != null) {
					val name = localisationInfo.name
					val category = localisationInfo.category
					val project = queryParameters.project
					//使用全部作用域
					val scope = GlobalSearchScope.allScope(project)
					//val scope = GlobalSearchScopeUtil.toGlobalSearchScope(queryParameters.scope, project)
					//这里不需要也无法进行排序
					val selector = localisationSelector().gameTypeFrom(sourceElement).preferRootFrom(sourceElement).preferLocale(preferredParadoxLocale())
					val localisations = when(category) {
						ParadoxLocalisationCategory.Localisation -> findLocalisations(name, project, scope, selector = selector)
						ParadoxLocalisationCategory.SyncedLocalisation -> findSyncedLocalisations(name, project, scope, selector = selector)
					}
					localisations.forEach {
						consumer.process(it)
					}
				}
			}
		}
		return true
	}
}