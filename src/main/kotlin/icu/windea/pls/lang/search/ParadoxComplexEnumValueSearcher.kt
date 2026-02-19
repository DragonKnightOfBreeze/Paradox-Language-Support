package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.index.ParadoxComplexEnumValueIndex
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.index.PlsIndexUtil
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.model.index.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 复杂枚举值的查询器。
 */
class ParadoxComplexEnumValueSearcher : QueryExecutorBase<ParadoxComplexEnumValueIndexInfo, ParadoxComplexEnumValueSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxComplexEnumValueSearch.SearchParameters, consumer: Processor<in ParadoxComplexEnumValueIndexInfo>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsStates.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType)
        if (SearchScope.isEmptyScope(scope)) return

        val keys = buildSet {
            add(createActualKey(queryParameters))
            add(PlsIndexUtil.createLazyKey())
        }
        PlsIndexService.processAllFileData(ParadoxComplexEnumValueIndex::class.java, keys, project, scope, queryParameters.gameType) { file, fileData ->
            val actualKey = createActualKey(queryParameters)
            val infos = fileData[actualKey].orEmpty()
            infos.process { info -> processInfo(queryParameters, file, info, consumer) }
        }
    }

    private fun createActualKey(queryParameters: ParadoxComplexEnumValueSearch.SearchParameters): String {
        val type = queryParameters.enumName
        return PlsIndexUtil.createTypeKey(type)
    }

    private fun processInfo(
        queryParameters: ParadoxComplexEnumValueSearch.SearchParameters,
        file: VirtualFile,
        info: ParadoxComplexEnumValueIndexInfo?,
        consumer: Processor<in ParadoxComplexEnumValueIndexInfo>
    ): Boolean {
        if (info == null) return true
        if (!matchesEnumName(queryParameters, info)) return true
        if (!matchesName(queryParameters, info)) return true
        info.bind(file, queryParameters.project)
        return consumer.process(info)
    }

    private fun matchesName(queryParameters: ParadoxComplexEnumValueSearch.SearchParameters, info: ParadoxComplexEnumValueIndexInfo): Boolean {
        if (queryParameters.name == null) return true
        return queryParameters.name.equals(info.name, info.caseInsensitive) // # 261
    }

    private fun matchesEnumName(queryParameters: ParadoxComplexEnumValueSearch.SearchParameters, info: ParadoxComplexEnumValueIndexInfo): Boolean {
        return queryParameters.enumName == info.enumName
    }
}
