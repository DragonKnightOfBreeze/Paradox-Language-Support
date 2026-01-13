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
import icu.windea.pls.model.index.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 复杂枚举值的查询器。
 */
class ParadoxComplexEnumValueSearcher : QueryExecutorBase<ParadoxComplexEnumValueIndexInfo, ParadoxComplexEnumValueSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxComplexEnumValueSearch.SearchParameters, consumer: Processor<in ParadoxComplexEnumValueIndexInfo>) {
        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType)
        if (SearchScope.isEmptyScope(scope)) return

        val gameType = queryParameters.selector.gameType
        val indexInfoType = ParadoxIndexInfoType.ComplexEnumValue
        PlsIndexService.processAllFileDataWithKey(indexInfoType, project, scope, gameType) { file, infos ->
            infos.process { info -> processInfo(queryParameters, info, file, consumer) }
        }
    }

    private fun processInfo(
        queryParameters: ParadoxComplexEnumValueSearch.SearchParameters,
        info: ParadoxComplexEnumValueIndexInfo,
        file: VirtualFile,
        consumer: Processor<in ParadoxComplexEnumValueIndexInfo>
    ): Boolean {
        if (queryParameters.enumName != info.enumName) return true
        if (queryParameters.name != null && !queryParameters.name.equals(info.name, info.caseInsensitive)) return true // # 261
        info.virtualFile = file
        return consumer.process(info)
    }
}

