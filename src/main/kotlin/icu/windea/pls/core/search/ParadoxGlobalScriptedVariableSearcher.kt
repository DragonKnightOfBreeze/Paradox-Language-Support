package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.script.psi.*

/**
 * 全局封装变量的查询器。
 */
class ParadoxGlobalScriptedVariableSearcher : QueryExecutorBase<ParadoxScriptScriptedVariable, ParadoxGlobalScriptedVariableSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxGlobalScriptedVariableSearch.SearchParameters, consumer: Processor<in ParadoxScriptScriptedVariable>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if(SearchScope.isEmptyScope(scope)) return
        val project = queryParameters.project
        
        val indexKey = ParadoxScriptedVariableNameIndex.KEY
        
        ProgressManager.checkCanceled()
        DumbService.getInstance(project).runReadActionInSmartMode action@{
            if(queryParameters.name == null) {
                //查找所有封装变量
                indexKey.processAllElementsByKeys(project, scope) { _, it ->
                    consumer.process(it)
                }
            } else {
                //查找指定名字的封装变量
                indexKey.processAllElements(queryParameters.name, project, scope) {
                    consumer.process(it)
                }
            }
        }
    }
}
