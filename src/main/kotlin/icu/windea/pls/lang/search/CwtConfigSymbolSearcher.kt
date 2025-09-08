package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.findFileBasedIndex
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.lang.index.CwtConfigSymbolIndex
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.indexInfo.CwtConfigSymbolIndexInfo

class CwtConfigSymbolSearcher : QueryExecutorBase<CwtConfigSymbolIndexInfo, CwtConfigSymbolSearch.SearchParameters>() {
    override fun processQuery(queryParameters: CwtConfigSymbolSearch.SearchParameters, consumer: Processor<in CwtConfigSymbolIndexInfo>) {
        ProgressManager.checkCanceled()
        if (queryParameters.project.isDefault) return
        val scope = queryParameters.scope
        if (SearchScope.isEmptyScope(scope)) return
        val name = queryParameters.name
        val types = queryParameters.types
        val gameType = queryParameters.gameType
        val project = queryParameters.project

        doProcessFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()
            //check game type at file level
            if (gameType != null) {
                val configGroup = CwtConfigManager.getContainingConfigGroup(file, project) ?: return@p true
                if (configGroup.gameType != ParadoxGameType.Core && configGroup.gameType != gameType) return@p true
            }

            val fileData = findFileBasedIndex<CwtConfigSymbolIndex>().getFileData(file, project)
            if (fileData.isEmpty()) return@p true
            types.forEach f@{ type ->
                val infos = fileData[type]
                if (infos.isNullOrEmpty()) return@f
                infos.forEach f@{ info ->
                    if (name != null && name != info.name) return@f
                    info.virtualFile = file
                    val r = consumer.process(info)
                    if (!r) return@p false
                }
            }

            true
        }
    }

    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>): Boolean {
        return FileTypeIndex.processFiles(CwtFileType, processor, scope)
    }
}
