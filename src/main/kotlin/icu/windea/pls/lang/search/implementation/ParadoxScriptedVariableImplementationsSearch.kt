package icu.windea.pls.lang.search.implementation

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.runReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.DefinitionsScopedSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScope
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import java.util.concurrent.Callable

/**
 * 封装变量的实现的查询。加入所有作用域内的同名封装变量。
 */
class ParadoxScriptedVariableImplementationsSearch : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
    override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
        // 得到解析后的 PSI 元素
        val sourceElement = queryParameters.element
        if (sourceElement !is ParadoxScriptScriptedVariable) return true
        val name = runReadAction { sourceElement.name }
        if (name.isNullOrEmpty()) return true
        val project = queryParameters.project
        val task = Callable {
            // 这里不进行排序
            val selector = selector(project, sourceElement).scriptedVariable()
                .withSearchScope(GlobalSearchScope.allScope(project)) // 使用全部作用域
            ParadoxScriptedVariableSearch.searchLocal(name, selector).forEach(consumer)
            ParadoxScriptedVariableSearch.searchGlobal(name, selector).forEach(consumer)
        }
        ReadAction.nonBlocking(task).inSmartMode(project).executeSynchronously()
        return true
    }
}
