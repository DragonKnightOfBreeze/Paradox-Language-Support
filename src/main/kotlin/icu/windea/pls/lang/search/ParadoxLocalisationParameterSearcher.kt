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
import icu.windea.pls.model.indexInfo.ParadoxLocalisationParameterIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

class ParadoxLocalisationParameterSearcher : QueryExecutorBase<ParadoxLocalisationParameterIndexInfo, ParadoxLocalisationParameterSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxLocalisationParameterSearch.SearchParameters, consumer: Processor<in ParadoxLocalisationParameterIndexInfo>) {
        ProgressManager.checkCanceled()
        if (queryParameters.project.isDefault) return
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val name = queryParameters.name
        val localisationName = queryParameters.localisationName
        val project = queryParameters.project
        val selector = queryParameters.selector
        val gameType = selector.gameType ?: return

        doProcessFiles(scope) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxCoreManager.getFileInfo(file) //ensure file info is resolved here
            if (selectGameType(file) != gameType) return@p true //check game type at file level

            val infos = ParadoxIndexInfoType.LocalisationParameter.findInfos(file, project)
            if (infos.isEmpty()) return@p true
            infos.forEach f@{ info ->
                if (localisationName != info.localisationName) return@f
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
