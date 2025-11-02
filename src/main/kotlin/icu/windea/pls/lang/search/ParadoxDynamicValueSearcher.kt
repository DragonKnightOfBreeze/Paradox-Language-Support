package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.lang.index.ParadoxIndexInfoType
import icu.windea.pls.lang.index.PlsIndexManager
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
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val gameType = queryParameters.selector.gameType ?: return

        val indexInfoType = ParadoxIndexInfoType.DynamicValue
        PlsIndexManager.processFiles(indexInfoType, ParadoxScriptFileType, project, gameType, scope) { file, infos ->
            ProgressManager.checkCanceled()
            infos.process { info -> processInfo(queryParameters, info, file, consumer) }
        }
        PlsIndexManager.processFiles(indexInfoType, ParadoxLocalisationFileType, project, gameType, scope) { file, infos ->
            ProgressManager.checkCanceled()
            infos.process { info -> processInfo(queryParameters, info, file, consumer) }
        }
    }

    private fun processInfo(
        queryParameters: ParadoxDynamicValueSearch.SearchParameters,
        info: ParadoxDynamicValueIndexInfo,
        file: VirtualFile,
        consumer: Processor<in ParadoxDynamicValueIndexInfo>
    ): Boolean {
        if (info.dynamicValueType !in queryParameters.dynamicValueTypes) return true
        if (queryParameters.name != null && queryParameters.name != info.name) return true
        info.virtualFile = file
        return consumer.process(info)
    }
}
