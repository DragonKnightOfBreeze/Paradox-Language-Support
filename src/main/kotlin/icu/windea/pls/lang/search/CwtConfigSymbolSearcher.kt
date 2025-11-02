package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.collections.process
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.index.PlsIndexManager
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

        val name = queryParameters.name
        val types = queryParameters.types
        val gameType = queryParameters.gameType

        val indexId = PlsIndexKeys.ConfigSymbol
        val keys = types
        PlsIndexManager.processFilesWithKeys(indexId, keys, scope) p@{ file ->
            ProgressManager.checkCanceled()
            // check game type at file level
            if (gameType != null) {
                val configGroup = CwtConfigManager.getContainingConfigGroup(file, project) ?: return@p true
                if (configGroup.gameType != ParadoxGameType.Core && configGroup.gameType != gameType) return@p true
            }

            val fileData = PlsIndexManager.getFileData(indexId, file, project)
            if (fileData.isEmpty()) return@p true
            types.process p1@{ type ->
                val infos = fileData[type]
                if (infos.isNullOrEmpty()) return@p1 true
                infos.process p2@{ info ->
                    if (name != null && name != info.name) return@p2 true
                    info.virtualFile = file
                    consumer.process(info)
                }
            }
        }
    }
}
