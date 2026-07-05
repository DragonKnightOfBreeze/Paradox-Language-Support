package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.lang.index.ChronicleIndexService
import icu.windea.pls.lang.search.ParadoxParameterSearch
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxIndexInfoTypes
import icu.windea.pls.model.index.ParadoxParameterIndexInfo

/**
 * 参数的查询器。
 */
class ParadoxParameterSearcher : QueryExecutorBase<ParadoxParameterIndexInfo, ParadoxParameterSearch.Parameters>() {
    override fun processQuery(queryParameters: ParadoxParameterSearch.Parameters, consumer: Processor<in ParadoxParameterIndexInfo>) {
        // 尽管新增了内联脚本传参的索引（ParadoxInlineScriptArgumentIndex），这里仍然统一通过合并索引（ParadoxMergedIndex）进行查询
        // 因为这里需要查询所有上下文的所有访问级别（读/写）的参数

        ProgressManager.checkCanceled()
        val context = queryParameters.createContext()
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, consumer: Processor<in ParadoxParameterIndexInfo>): Boolean {
        if (!context.isValid()) return true
        val indexInfoType = ParadoxIndexInfoTypes.Parameter
        return ChronicleIndexService.processAllFileDataWithKey(indexInfoType, context.project, context.scope, context.gameType) { file, infos ->
            infos.process { info -> processInfo(context, file, info, consumer) }
        }
    }

    private fun processInfo(context: Context, file: VirtualFile, info: ParadoxParameterIndexInfo, consumer: Processor<in ParadoxParameterIndexInfo>): Boolean {
        if (!matchesContextKey(context, info)) return true
        if (!matchesName(context, info)) return true
        info.bind(file, context.project)
        return consumer.process(info)
    }

    private fun matchesContextKey(context: Context, info: ParadoxParameterIndexInfo): Boolean {
        return context.contextKey == info.contextKey
    }

    private fun matchesName(context: Context, info: ParadoxParameterIndexInfo): Boolean {
        if (context.name == null) return true
        return context.name == info.name
    }

    private fun ParadoxParameterSearch.Parameters.createContext(scope: GlobalSearchScope = this.scope): Context {
        return Context(name, contextKey, gameType, project, scope)
    }

    private data class Context(
        val name: String?,
        val contextKey: String,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext
}
