package icu.windea.pls.lang.search.implementation

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.runReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.DefinitionsScopedSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScope
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import java.util.concurrent.Callable

/**
 * 本地化的实现的查询。加入所有作用域内的包括不同语言环境在内的同名本地化。
 */
class ParadoxLocalisationImplementationsSearch : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
    override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
        // 得到解析后的 PSI 元素
        val sourceElement = queryParameters.element
        if (sourceElement !is ParadoxLocalisationProperty) return true
        val name = runReadAction { sourceElement.name.orNull() } ?: return true
        val type = runReadAction { sourceElement.type } ?: return true
        if (name.isEmpty()) return true
        val project = queryParameters.project
        val task = Callable {
            // 这里不进行排序
            val selector = selector(project, sourceElement).localisation()
                .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig()) // 限定语言环境
                .withSearchScope(GlobalSearchScope.allScope(project)) // 使用全部作用域
            ParadoxLocalisationSearch.search(name, type, selector).forEach(consumer)
        }
        ReadAction.nonBlocking(task).inSmartMode(project).executeSynchronously()
        return true
    }
}
