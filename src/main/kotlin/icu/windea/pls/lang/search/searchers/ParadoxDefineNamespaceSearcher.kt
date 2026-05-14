package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.Processor
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.ParadoxDefineNamespaceSearch.*
import icu.windea.pls.lang.search.scope.withFilePath
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定值命名空间的查询器。
 */
class ParadoxDefineNamespaceSearcher : QueryExecutorBase<ParadoxScriptProperty, Parameters>() {
    override fun processQuery(queryParameters: Parameters, consumer: Processor<in ParadoxScriptProperty>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsStates.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType).withFilePath("common/defines", "txt") // optimized
        val context = queryParameters.createContext(scope)
        if (!context.isValid()) return
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, consumer: Processor<in ParadoxScriptProperty>) {
        if (context.namespace == null) {
            PlsIndexService.processElementsByKeys(PlsIndexKeys.DefineNamespace, context.project, context.scope) { _, element ->
                consumer.process(element)
            }
        } else {
            if (context.namespace.isEmpty()) return
            PlsIndexService.processElements(PlsIndexKeys.DefineNamespace, context.namespace, context.project, context.scope) { element ->
                consumer.process(element)
            }
        }
    }
}
