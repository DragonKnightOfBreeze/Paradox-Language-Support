package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.ParadoxShaderEffectSearch.*
import icu.windea.pls.model.index.ParadoxIndexInfoTypes
import icu.windea.pls.model.index.ParadoxShaderEffectIndexInfo

/**
 * 着色器效果（shader effect）的查询器。
 */
class ParadoxShaderEffectSearcher : QueryExecutorBase<ParadoxShaderEffectIndexInfo, Parameters>() {
    override fun processQuery(queryParameters: Parameters, consumer: Processor<in ParadoxShaderEffectIndexInfo>) {
        ProgressManager.checkCanceled()
        val context = queryParameters.createContext()
        if (!context.isValid()) return
        processInternal(context, consumer)
    }

    private fun processInternal(context: Context, consumer: Processor<in ParadoxShaderEffectIndexInfo>) {
        val indexInfoType = ParadoxIndexInfoTypes.ShaderEffect
        PlsIndexService.processAllFileDataWithKey(indexInfoType, context.project, context.scope, context.gameType) { file, infos ->
            infos.process { info -> processInfo(context, file, info, consumer) }
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
}
