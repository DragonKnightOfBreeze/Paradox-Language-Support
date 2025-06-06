package icu.windea.pls.lang.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.ep.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.*

class ParadoxParameterSearcher : QueryExecutorBase<ParadoxParameterIndexInfo, ParadoxParameterSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxParameterSearch.SearchParameters, consumer: Processor<in ParadoxParameterIndexInfo>) {
        ProgressManager.checkCanceled()
        if(queryParameters.project.isDefault) return
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val name = queryParameters.name
        val contextKey = queryParameters.contextKey
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType ?: return

        doProcessFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxCoreManager.getFileInfo(file) //ensure file info is resolved here
            if (selectGameType(file) != gameType) return@p true //check game type at file level

            val fileData = ParadoxIndexManager.Merged.getFileData(file, project, ParadoxIndexInfoType.Parameter)
            if (fileData.isEmpty()) return@p true
            fileData.forEach f@{ info ->
                if (contextKey != info.contextKey) return@f
                if (name != null && name != info.name) return@f
                info.virtualFile = file
                val r = consumer.process(info)
                if (!r) return@p false
            }

            true
        }
    }

    private fun doProcessFiles(scope: GlobalSearchScope, processor: Processor<VirtualFile>): Boolean {
        return FileTypeIndex.processFiles(ParadoxScriptFileType, processor, scope)
    }
}
