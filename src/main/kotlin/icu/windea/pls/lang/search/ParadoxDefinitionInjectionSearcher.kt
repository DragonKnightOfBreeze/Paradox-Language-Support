package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.index.ParadoxDefinitionInjectionIndex
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.index.PlsIndexUtil
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.model.index.ParadoxDefinitionInjectionIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 定义注入的查询器。
 */
class ParadoxDefinitionInjectionSearcher : QueryExecutorBase<ParadoxDefinitionInjectionIndexInfo, ParadoxDefinitionInjectionSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDefinitionInjectionSearch.SearchParameters, consumer: Processor<in ParadoxDefinitionInjectionIndexInfo>) {
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
        PlsIndexService.processAllFileData(ParadoxDefinitionInjectionIndex::class.java, keys, project, scope, queryParameters.gameType) p@{ file, fileData ->
            val actualKey = createActualKey(queryParameters)
            val infos = fileData[actualKey].orEmpty()
            infos.process { info -> processInfo(queryParameters, file, info, consumer) }
        }
    }

    private fun createActualKey(queryParameters: ParadoxDefinitionInjectionSearch.SearchParameters): String {
        val name = queryParameters.target
        val type = queryParameters.type
        return when {
            !name.isNullOrEmpty() && !type.isNullOrEmpty() -> PlsIndexUtil.createNameTypeKey(name, type)
            !name.isNullOrEmpty() -> PlsIndexUtil.createNameKey(name)
            !type.isNullOrEmpty() -> PlsIndexUtil.createTypeKey(type)
            else -> PlsIndexUtil.createAllKey()
        }
    }

    private fun processInfo(
        queryParameters: ParadoxDefinitionInjectionSearch.SearchParameters,
        file: VirtualFile,
        info: ParadoxDefinitionInjectionIndexInfo,
        consumer: Processor<in ParadoxDefinitionInjectionIndexInfo>
    ): Boolean {
        if (!matchesMode(queryParameters, info)) return true
        if (!matchesTarget(queryParameters, info)) return true
        if (!matchesType(queryParameters, info)) return true
        info.bind(file, queryParameters.project)
        return consumer.process(info)
    }

    private fun matchesMode(queryParameters: ParadoxDefinitionInjectionSearch.SearchParameters, info: ParadoxDefinitionInjectionIndexInfo): Boolean {
        if (queryParameters.mode == null) return true
        return queryParameters.mode.equals(info.mode, true)
    }

    private fun matchesTarget(queryParameters: ParadoxDefinitionInjectionSearch.SearchParameters, info: ParadoxDefinitionInjectionIndexInfo): Boolean {
        if (queryParameters.target == null) return true
        return queryParameters.target == info.target
    }

    private fun matchesType(queryParameters: ParadoxDefinitionInjectionSearch.SearchParameters, info: ParadoxDefinitionInjectionIndexInfo): Boolean {
        if (queryParameters.type == null) return true
        return queryParameters.type == info.type
    }
}
