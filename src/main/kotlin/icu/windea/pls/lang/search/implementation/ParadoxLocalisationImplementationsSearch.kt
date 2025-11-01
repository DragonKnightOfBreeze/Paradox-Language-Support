package icu.windea.pls.lang.search.implementation

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.runReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.DefinitionsScopedSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.localisationInfo
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxSyncedLocalisationSearch
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScope
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import java.util.concurrent.Callable

/**
 * 本地化的实现的查询。加入所有作用域内的包括不同语言环境在内的同名本地化。
 */
class ParadoxLocalisationImplementationsSearch : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
    override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
        // 得到解析后的PSI元素
        val sourceElement = queryParameters.element
        if (sourceElement !is ParadoxLocalisationProperty) return true
        val localisationInfo = runReadAction { sourceElement.localisationInfo }
        if (localisationInfo == null) return true
        val name = localisationInfo.name
        if (name.isEmpty()) return true
        val project = queryParameters.project
        val task = Callable {
            // 这里不进行排序
            val selector = selector(project, sourceElement).localisation()
                .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig()) // 限定语言环境
                .withSearchScope(GlobalSearchScope.allScope(project)) // 使用全部作用域
            val query = when (localisationInfo.type) {
                ParadoxLocalisationType.Normal -> ParadoxLocalisationSearch.search(name, selector)
                ParadoxLocalisationType.Synced -> ParadoxSyncedLocalisationSearch.search(name, selector)
            }
            query.forEach(consumer)
        }
        ReadAction.nonBlocking(task).inSmartMode(project).executeSynchronously()
        return true
    }
}
