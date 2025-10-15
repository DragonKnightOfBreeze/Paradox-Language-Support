package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.lang.index.ParadoxIndexInfoType
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxCoreManager
import icu.windea.pls.model.indexInfo.ParadoxParameterIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

class ParadoxParameterSearcher : QueryExecutorBase<ParadoxParameterIndexInfo, ParadoxParameterSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxParameterSearch.SearchParameters, consumer: Processor<in ParadoxParameterIndexInfo>) {
        ProgressManager.checkCanceled()
        if (queryParameters.project.isDefault) return
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val name = queryParameters.name
        val contextKey = queryParameters.contextKey
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType

        // 尽管新增了内联脚本传参的索引（ParadoxInlineScriptArgumentIndex），这里仍然统一通过合并索引（ParadoxMergedIndex）进行查询
        // 因为这里需要查询所有上下文的所有访问级别（读/写）的参数

        processFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxCoreManager.getFileInfo(file) // ensure file info is resolved here
            if (gameType != null && selectGameType(file) != gameType) return@p true // check game type at file level

            val infos = ParadoxIndexInfoType.Parameter.findInfos(file, project)
            if (infos.isEmpty()) return@p true
            infos.forEach f@{ info ->
                if (contextKey != info.contextKey) return@f
                if (name != null && name != info.name) return@f
                info.virtualFile = file
                val r = consumer.process(info)
                if (!r) return@p false
            }

            true
        }
    }

    private fun processFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>): Boolean {
        return FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
    }
}
