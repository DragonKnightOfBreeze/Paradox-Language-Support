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

/**
 * 本地化的实现的查询。加入所有作用域内的包括不同语言区域在内的同名本地化。
 */
class ParadoxLocalisationImplementationsSearch : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
    override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
        //得到解析后的PSI元素
        val sourceElement = queryParameters.element
        if (sourceElement !is ParadoxLocalisationProperty) return true
        val localisationInfo = runReadAction { sourceElement.localisationInfo }
        if (localisationInfo == null) return true
        val name = localisationInfo.name
        if (name.isEmpty()) return true
        val project = queryParameters.project
        ReadAction.nonBlocking<Unit> {
            val type = localisationInfo.type
            //这里不需要也无法进行排序
            val selector = selector(project, sourceElement).localisation()
                .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig()) //限定语言区域
                .withSearchScope(GlobalSearchScope.allScope(project)) //使用全部作用域
            val localisations = when (type) {
                ParadoxLocalisationType.Normal -> ParadoxLocalisationSearch.search(name, selector).findAll()
                ParadoxLocalisationType.Synced -> ParadoxSyncedLocalisationSearch.search(name, selector).findAll()
            }
            localisations.forEach {
                consumer.process(it)
            }
        }.inSmartMode(project).executeSynchronously()
        return true
    }
}
