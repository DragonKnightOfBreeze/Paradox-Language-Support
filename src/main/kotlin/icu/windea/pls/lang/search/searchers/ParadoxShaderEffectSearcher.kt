package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.processFast
import icu.windea.pls.lang.index.ChronicleIndexService
import icu.windea.pls.lang.index.ParadoxMergedIndexTypes
import icu.windea.pls.lang.search.ParadoxShaderEffectSearch
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxShaderEffectIndexInfo

/**
 * 着色器效果（shader effect）的查询器。
 */
@Optimized
class ParadoxShaderEffectSearcher : QueryExecutorBase<ParadoxShaderEffectIndexInfo, ParadoxShaderEffectSearch.Parameters>() {
    override fun processQuery(queryParameters: ParadoxShaderEffectSearch.Parameters, consumer: Processor<in ParadoxShaderEffectIndexInfo>) {
        ProgressManager.checkCanceled()
        val context = queryParameters.createContext()
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, consumer: Processor<in ParadoxShaderEffectIndexInfo>): Boolean {
        if (!context.isValid()) return true
        val mergedIndexType = ParadoxMergedIndexTypes.ShaderEffect
        return ChronicleIndexService.processAllFileDataWithKey(mergedIndexType, context.project, context.scope, context.gameType) { file, infos ->
            infos.processFast { info -> processInfo(context, file, info, consumer) }
        }
    }

    private fun processInfo(context: Context, file: VirtualFile, info: ParadoxShaderEffectIndexInfo, consumer: Processor<in ParadoxShaderEffectIndexInfo>): Boolean {
        if (!matchesName(context, info)) return true
        info.bind(file, context.project)
        return consumer.process(info)
    }

    private fun matchesName(context: Context, info: ParadoxShaderEffectIndexInfo): Boolean {
        if (context.name == null) return true
        return context.name == info.name
    }

    private fun ParadoxShaderEffectSearch.Parameters.createContext(scope: GlobalSearchScope = this.scope): Context {
        return Context(name, gameType, project, scope)
    }

    private data class Context(
        val name: String?,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext
}
