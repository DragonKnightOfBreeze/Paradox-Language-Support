package icu.windea.pls.lang.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.cwt.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.model.indexInfo.*

class CwtConfigSymbolSearcher : QueryExecutorBase<CwtConfigSymbolIndexInfo, CwtConfigSymbolSearch.SearchParameters>() {
    override fun processQuery(queryParameters: CwtConfigSymbolSearch.SearchParameters, consumer: Processor<in CwtConfigSymbolIndexInfo>) {
        ProgressManager.checkCanceled()
        if (queryParameters.project.isDefault) return
        val scope = queryParameters.scope
        if (SearchScope.isEmptyScope(scope)) return
        val name = queryParameters.name
        val type = queryParameters.type
        val project = queryParameters.project

        doProcessFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()

            val fileData = CwtConfigIndexManager.Symbol.getFileData(file, project)
            if (fileData.isEmpty()) return@p true
            val infos = fileData[type]
            if (infos.isNullOrEmpty()) return@p true
            infos.forEach f@{ info ->
                if (name != null && name != info.name) return@f
                info.virtualFile = file
                val r = consumer.process(info)
                if (!r) return@p false
            }

            true
        }
    }

    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>): Boolean {
        return FileTypeIndex.processFiles(CwtFileType, processor, scope)
    }
}
