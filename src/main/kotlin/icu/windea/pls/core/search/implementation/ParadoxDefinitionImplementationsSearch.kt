package icu.windea.pls.core.search.implementation

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.script.psi.*

/**
 * 定义的实现的查询。加入所有作用域内的同名定义。
 *
 * @see icu.windea.pls.script.intentions.DefinitionNameGotoImplementationsIntention
 */
class ParadoxDefinitionImplementationsSearch : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
    override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
        //得到解析后的PSI元素
        val sourceElement = queryParameters.element
        if(sourceElement !is ParadoxScriptDefinitionElement) return true
        val definitionInfo = runReadAction { sourceElement.definitionInfo }
        if(definitionInfo == null) return true
        val name = definitionInfo.name
        if(name.isEmpty()) return true
        val type = definitionInfo.type
        val project = queryParameters.project
        DumbService.getInstance(project).runReadActionInSmartMode {
            //这里不进行排序
            val selector = definitionSelector(project, sourceElement)
                .withSearchScope(GlobalSearchScope.allScope(project)) //使用全部作用域
            ParadoxDefinitionSearch.search(name, type, selector).forEach(consumer)
        }
        return true
    }
}

