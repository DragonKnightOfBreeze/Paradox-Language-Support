package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.processAllElements
import icu.windea.pls.core.processAllElementsByKeys
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.lang.search.scope.withFilePath
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 全局封装变量的查询器。
 *
 * 全局封装变量：位于特定位置（`common/scripted_variables/**/*.txt`）的脚本文件中的封装变量。
 * -
 */
class ParadoxGlobalScriptedVariableSearcher : QueryExecutorBase<ParadoxScriptScriptedVariable, ParadoxGlobalScriptedVariableSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxGlobalScriptedVariableSearch.SearchParameters, consumer: Processor<in ParadoxScriptScriptedVariable>) {
        //#141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if(PlsCoreManager.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        if(queryParameters.project.isDefault) return
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
            return ParadoxIndexKeys.ScriptedVariableName.processAllElementsByKeys(project, scope) { _, element -> processor.process(element) }
        } else {
            return ParadoxIndexKeys.ScriptedVariableName.processAllElements(name, project, scope) { element -> processor.process(element) }
        }
    }
}
