package icu.windea.pls.lang.search.implementation

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.script.psi.*

/**
 * 封装变量的实现的查询。加入所有作用域内的同名封装变量。
 */
class ParadoxScriptedVariableImplementationsSearch : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
    override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
        //得到解析后的PSI元素
        val sourceElement = queryParameters.element
        if(sourceElement !is ParadoxScriptScriptedVariable) return true
        val name = runReadAction { sourceElement.name }
        if(name.isNullOrEmpty()) return true
        val project = queryParameters.project
        DumbService.getInstance(project).runReadActionInSmartMode {
            //这里不进行排序
            //使用全部作用域
            val selector = scriptedVariableSelector(project, sourceElement)
                .withSearchScope(GlobalSearchScope.allScope(project))
            ParadoxLocalScriptedVariableSearch.search(name, selector).forEach(consumer)
            ParadoxGlobalScriptedVariableSearch.search(name, selector).forEach(consumer)
        }
        return true
    }
}
