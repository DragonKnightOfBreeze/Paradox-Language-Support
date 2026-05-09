package icu.windea.pls.lang.search.searchers.implementations

import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.DefinitionsScopedSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.lang.defineVariableInfo
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScope
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定值变量的实现的查询器。加入所有作用域内的同名定值变量。
 */
class ParadoxDefineVariableImplementationsSearcher : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
    override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
        val project = queryParameters.project
        val sourceElement = queryParameters.element
        if (sourceElement !is ParadoxScriptProperty) return true

        runSmartReadAction(project, inSmartMode = true) action@{
            val defineVariableInfo = sourceElement.defineVariableInfo ?: return@action true
            val namespace = defineVariableInfo.namespace.orNull() ?: return@action true
            val variable = defineVariableInfo.variable.orNull() ?: return@action true
            // 这里不进行排序
            val selector = selector(project, sourceElement).define()
                .withSearchScope(GlobalSearchScope.allScope(project)) // 使用全部作用域
            ParadoxDefineVariableSearch.search(namespace, variable, selector).forEach(consumer)
        }
        return true
    }
}
