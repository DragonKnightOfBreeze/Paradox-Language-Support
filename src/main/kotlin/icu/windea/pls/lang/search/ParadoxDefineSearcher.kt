package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.scope.withFilePath
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.model.index.ParadoxDefineVariableKey
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 预定义的命名空间与变量的查询器。
 */
class ParadoxDefineSearcher : QueryExecutorBase<ParadoxScriptProperty, ParadoxDefineSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDefineSearch.SearchParameters, consumer: Processor<in ParadoxScriptProperty>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsStates.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType).withFilePath("common/defines", "txt") // optimized
        if (SearchScope.isEmptyScope(scope)) return

        val variable = queryParameters.variable
        val namespace = queryParameters.namespace

        processQueryForDefines(namespace, variable, project, scope, consumer)
    }

    private fun processQueryForDefines(
        namespace: String?,
        variable: String?,
        project: Project,
        scope: GlobalSearchScope,
        consumer: Processor<in ParadoxScriptProperty>
    ) {
        when {
            namespace != null && variable != null -> {
                if (variable.isEmpty()) {
                    // namespace only
                    PlsIndexService.processElements(PlsIndexKeys.DefineNamespace, namespace, project, scope) { element ->
                        consumer.process(element)
                    }
                } else {
                    val key = ParadoxDefineVariableKey(namespace, variable)
                    PlsIndexService.processElements(PlsIndexKeys.DefineVariable, key, project, scope) { element ->
                        consumer.process(element)
                    }
                }
            }
            namespace != null -> {
                // namespace specified, query all variables under it + namespace element
                PlsIndexService.processElements(PlsIndexKeys.DefineNamespace, namespace, project, scope) { element ->
                    consumer.process(element)
                }
                PlsIndexService.processElementsByKeys(PlsIndexKeys.DefineVariable, project, scope, { it.namespace == namespace }) { _, element ->
                    consumer.process(element)
                }
            }
            variable != null -> {
                if (variable.isEmpty()) {
                    // all namespaces
                    PlsIndexService.processElementsByKeys(PlsIndexKeys.DefineNamespace, project, scope) { _, element ->
                        consumer.process(element)
                    }
                } else {
                    // variable specified but namespace not specified
                    PlsIndexService.processElementsByKeys(PlsIndexKeys.DefineVariable, project, scope, { it.variable == variable }) { _, element ->
                        consumer.process(element)
                    }
                }
            }
            else -> {
                // all defines
                PlsIndexService.processElementsByKeys(PlsIndexKeys.DefineNamespace, project, scope) { _, element ->
                    consumer.process(element)
                }
                PlsIndexService.processElementsByKeys(PlsIndexKeys.DefineVariable, project, scope) { _, element ->
                    consumer.process(element)
                }
            }
        }
    }
}
