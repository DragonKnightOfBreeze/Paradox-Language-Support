package icu.windea.pls.lang.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.search.scope.*
import icu.windea.pls.script.psi.*

/**
 * 全局封装变量的查询器。
 * 
 * 全局封装变量：位于`common/scripted_variables`目录中的脚本文件中的封装变量。
 */
class ParadoxGlobalScriptedVariableSearcher : QueryExecutorBase<ParadoxScriptScriptedVariable, ParadoxGlobalScriptedVariableSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxGlobalScriptedVariableSearch.SearchParameters, consumer: Processor<in ParadoxScriptScriptedVariable>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
            .withFilePath("common/scripted_variables", "txt") //limit to global scripted variables
        if (SearchScope.isEmptyScope(scope)) return
        val project = queryParameters.project
        val name = queryParameters.name
        doProcessAllElements(name, project, scope) { element ->
            consumer.process(element)
        }
    }

    private fun doProcessAllElements(name: String?, project: Project, scope: GlobalSearchScope, processor: Processor<ParadoxScriptScriptedVariable>): Boolean {
        if (name == null) {
            return ParadoxScriptedVariableNameIndex.KEY.processAllElementsByKeys(project, scope) { _, element -> processor.process(element) }
        } else {
            return ParadoxScriptedVariableNameIndex.KEY.processAllElements(name, project, scope) { element -> processor.process(element) }
        }
    }
}
