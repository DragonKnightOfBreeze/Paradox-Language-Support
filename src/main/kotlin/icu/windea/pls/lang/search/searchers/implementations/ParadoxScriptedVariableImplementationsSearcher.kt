package icu.windea.pls.lang.search.searchers.implementations

import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.DefinitionsScopedSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScope
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 封装变量的实现的查询器。加入所有作用域内的同名封装变量。
 */
class ParadoxScriptedVariableImplementationsSearcher : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
    override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
        val project = queryParameters.project
        val sourceElement = queryParameters.element
        if (sourceElement !is ParadoxScriptScriptedVariable) return true

        runSmartReadAction(project, inSmartMode = true) action@{
            val name = sourceElement.name?.orNull() ?: return@action null
            // 这里不进行排序
            val selector = selector(project, sourceElement).scriptedVariable()
                .withSearchScope(GlobalSearchScope.allScope(project)) // 使用全部作用域
            ParadoxScriptedVariableSearch.searchLocal(name, selector).forEach(consumer)
            ParadoxScriptedVariableSearch.searchGlobal(name, selector).forEach(consumer)
        }
        return true
    }
}
