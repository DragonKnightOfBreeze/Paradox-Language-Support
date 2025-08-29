package icu.windea.pls.lang.search.implementation

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.runReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.DefinitionsScopedSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.ParadoxGlobalScriptedVariableSearch
import icu.windea.pls.lang.search.ParadoxLocalScriptedVariableSearch
import icu.windea.pls.lang.search.selector.scriptedVariable
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScope
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 封装变量的实现的查询。加入所有作用域内的同名封装变量。
 */
class ParadoxScriptedVariableImplementationsSearch : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
    override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
        //得到解析后的PSI元素
        val sourceElement = queryParameters.element
        if (sourceElement !is ParadoxScriptScriptedVariable) return true
        val name = runReadAction { sourceElement.name }
        if (name.isNullOrEmpty()) return true
        val project = queryParameters.project
        ReadAction.nonBlocking<Unit> {
            //这里不进行排序
            //使用全部作用域
            val selector = selector(project, sourceElement).scriptedVariable().withSearchScope(GlobalSearchScope.allScope(project))
            ParadoxLocalScriptedVariableSearch.search(name, selector).forEach(consumer)
            ParadoxGlobalScriptedVariableSearch.search(name, selector).forEach(consumer)
        }.inSmartMode(project).executeSynchronously()
        return true
    }
}
