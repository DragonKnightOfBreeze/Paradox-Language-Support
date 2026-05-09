package icu.windea.pls.lang.search.searchers.implementations

import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.DefinitionsScopedSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScope
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * 定义的实现的查询器。加入所有作用域内的同名定义。
 */
class ParadoxDefinitionImplementationsSearcher : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
    override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
        val project = queryParameters.project
        val sourceElement = queryParameters.element
        if (sourceElement !is ParadoxDefinitionElement) return true

        runSmartReadAction(project, inSmartMode = true) action@{
            val definitionInfo = sourceElement.definitionInfo ?: return@action true
            val name = definitionInfo.name.orNull() ?: return@action true
            val type = definitionInfo.type.orNull() ?: return@action true
            // 这里不进行排序
            val selector = selector(project, sourceElement).definition()
                .withSearchScope(GlobalSearchScope.allScope(project)) // 使用全部作用域
            ParadoxDefinitionSearch.searchElement(name, type, selector).forEach(consumer)
        }
        return true
    }
}
