package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.collections.process
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.CwtConfigSymbolIndexInfo

class CwtConfigSymbolSearcher : QueryExecutorBase<CwtConfigSymbolIndexInfo, CwtConfigSymbolSearch.SearchParameters>() {
    override fun processQuery(queryParameters: CwtConfigSymbolSearch.SearchParameters, consumer: Processor<in CwtConfigSymbolIndexInfo>) {
        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = queryParameters.scope.withFileTypes(CwtFileType)
        if (SearchScope.isEmptyScope(scope)) return

        val indexId = PlsIndexKeys.ConfigSymbol
        val keys = queryParameters.types
        PlsIndexService.processFiles(indexId, keys, project, scope) p@{ file ->
            ProgressManager.checkCanceled()
            // check game type at file level
            if (queryParameters.gameType != null) {
                val configGroup = CwtConfigManager.getContainingConfigGroup(file, project) ?: return@p true
                if (configGroup.gameType != ParadoxGameType.Core && configGroup.gameType != queryParameters.gameType) return@p true
            }

            val fileData = FileBasedIndex.getInstance().getFileData(indexId, file, project)
            if (fileData.isEmpty()) return@p true
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
        if (queryParameters.name != null && queryParameters.name != info.name) return true
        info.bind(file, queryParameters.project)
        return consumer.process(info)
    }
}
