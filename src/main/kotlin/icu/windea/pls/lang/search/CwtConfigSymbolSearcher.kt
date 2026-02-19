package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.lang.index.CwtConfigSymbolIndex
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.model.index.CwtConfigSymbolIndexInfo

class CwtConfigSymbolSearcher : QueryExecutorBase<CwtConfigSymbolIndexInfo, CwtConfigSymbolSearch.SearchParameters>() {
    override fun processQuery(queryParameters: CwtConfigSymbolSearch.SearchParameters, consumer: Processor<in CwtConfigSymbolIndexInfo>) {
        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = queryParameters.scope.withFileTypes(CwtFileType)
        if (SearchScope.isEmptyScope(scope)) return

        val keys = queryParameters.types
        PlsIndexService.processAllFileData(CwtConfigSymbolIndex::class.java, keys, project, scope, queryParameters.gameType) { file, fileData ->
            queryParameters.types.process { type ->
                val infos = fileData[type].orEmpty()
                infos.process { info -> processInfo(queryParameters, file, info, consumer) }
            }
        }
    }

    private fun processInfo(
        queryParameters: CwtConfigSymbolSearch.SearchParameters,
        file: VirtualFile,
        info: CwtConfigSymbolIndexInfo?,
        consumer: Processor<in CwtConfigSymbolIndexInfo>
    ): Boolean {
        if (info == null) return true
        if (!matchesName(queryParameters, info)) return true
        info.bind(file, queryParameters.project)
        return consumer.process(info)
    }

    private fun matchesName(queryParameters: CwtConfigSymbolSearch.SearchParameters, info: CwtConfigSymbolIndexInfo): Boolean {
        if (queryParameters.name == null) return true
        return queryParameters.name == info.name
    }
}
