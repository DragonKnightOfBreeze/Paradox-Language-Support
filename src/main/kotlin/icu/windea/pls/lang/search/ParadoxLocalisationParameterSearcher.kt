package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.lang.index.ParadoxIndexInfoType
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.model.index.ParadoxLocalisationParameterIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 本地化参数的查询器。
 */
class ParadoxLocalisationParameterSearcher : QueryExecutorBase<ParadoxLocalisationParameterIndexInfo, ParadoxLocalisationParameterSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxLocalisationParameterSearch.SearchParameters, consumer: Processor<in ParadoxLocalisationParameterIndexInfo>) {
        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType)
        if (SearchScope.isEmptyScope(scope)) return

        val gameType = queryParameters.selector.gameType
        val indexInfoType = ParadoxIndexInfoType.LocalisationParameter
        PlsIndexService.processAllFileDataWithKey(indexInfoType, project, scope, gameType) { file, infos ->
            infos.process { info -> processInfo(queryParameters, file, info, consumer) }
        }
    }

    private fun processInfo(
        queryParameters: ParadoxLocalisationParameterSearch.SearchParameters,
        file: VirtualFile,
        info: ParadoxLocalisationParameterIndexInfo,
        consumer: Processor<in ParadoxLocalisationParameterIndexInfo>
    ): Boolean {
        if (queryParameters.localisationName != info.localisationName) return true
        if (queryParameters.name != null && queryParameters.name != info.name) return true
        info.virtualFile = file
        return consumer.process(info)
    }
}
