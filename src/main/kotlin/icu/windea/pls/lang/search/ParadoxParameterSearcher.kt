package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.lang.index.ParadoxIndexInfoType
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.model.index.ParadoxParameterIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 参数的查询器。
 */
class ParadoxParameterSearcher : QueryExecutorBase<ParadoxParameterIndexInfo, ParadoxParameterSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxParameterSearch.SearchParameters, consumer: Processor<in ParadoxParameterIndexInfo>) {
        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType)
        if (SearchScope.isEmptyScope(scope)) return

        // 尽管新增了内联脚本传参的索引（ParadoxInlineScriptArgumentIndex），这里仍然统一通过合并索引（ParadoxMergedIndex）进行查询
        // 因为这里需要查询所有上下文的所有访问级别（读/写）的参数

        val indexInfoType = ParadoxIndexInfoType.Parameter
        PlsIndexService.processAllFileDataWithKey(indexInfoType, project, scope, queryParameters.gameType) { file, infos ->
            infos.process { info -> processInfo(queryParameters, file, info, consumer) }
        }
    }

    private fun processInfo(
        queryParameters: ParadoxParameterSearch.SearchParameters,
        file: VirtualFile,
        info: ParadoxParameterIndexInfo,
        consumer: Processor<in ParadoxParameterIndexInfo>
    ): Boolean {
        if (queryParameters.contextKey != info.contextKey) return true
        if (queryParameters.name != null && queryParameters.name != info.name) return true
        info.bind(file, queryParameters.project)
        return consumer.process(info)
    }
}
