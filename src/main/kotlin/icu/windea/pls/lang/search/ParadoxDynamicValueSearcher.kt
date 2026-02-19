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
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.model.index.ParadoxDynamicValueIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 动态值的查询器。
 */
class ParadoxDynamicValueSearcher : QueryExecutorBase<ParadoxDynamicValueIndexInfo, ParadoxDynamicValueSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDynamicValueSearch.SearchParameters, consumer: Processor<in ParadoxDynamicValueIndexInfo>) {
        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType, ParadoxLocalisationFileType)
        if (SearchScope.isEmptyScope(scope)) return

        val indexInfoType = ParadoxIndexInfoType.DynamicValue
        PlsIndexService.processAllFileDataWithKey(indexInfoType, project, scope, queryParameters.gameType) { file, infos ->
            infos.process { info -> processInfo(queryParameters, file, info, consumer) }
        }
    }

    private fun processInfo(
        queryParameters: ParadoxDynamicValueSearch.SearchParameters,
        file: VirtualFile,
        info: ParadoxDynamicValueIndexInfo,
        consumer: Processor<in ParadoxDynamicValueIndexInfo>
    ): Boolean {
        if (!matchesType(queryParameters, info)) return true
        if (!matchesName(queryParameters, info)) return true
        info.bind(file, queryParameters.project)
        return consumer.process(info)
    }

    private fun matchesName(queryParameters: ParadoxDynamicValueSearch.SearchParameters, info: ParadoxDynamicValueIndexInfo): Boolean {
        if (queryParameters.name == null) return true
        return queryParameters.name == info.name
    }

    private fun matchesType(queryParameters: ParadoxDynamicValueSearch.SearchParameters, info: ParadoxDynamicValueIndexInfo): Boolean {
        return queryParameters.dynamicValueTypes.contains(info.dynamicValueType)
    }
}
