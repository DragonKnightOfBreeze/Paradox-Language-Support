package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.index.ParadoxDefinitionIndex
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.index.PlsIndexUtil
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType

/**
 * 定义的查询器。
 */
class ParadoxDefinitionSearcher : QueryExecutorBase<ParadoxDefinitionIndexInfo, ParadoxDefinitionSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDefinitionSearch.SearchParameters, consumer: Processor<in ParadoxDefinitionIndexInfo>) {
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
        PlsIndexService.processAllFileData(ParadoxDefinitionIndex::class.java, keys, project, scope, queryParameters.gameType) p@{ file, fileData ->
            val actualKey = createActualKey(queryParameters)
            val infos = fileData[actualKey].orEmpty()
            infos.process { info -> processInfo(queryParameters, file, info, consumer) }
        }
    }

    private fun createActualKey(queryParameters: ParadoxDefinitionSearch.SearchParameters) : String {
        return when {
            !queryParameters.name.isNullOrEmpty() && !queryParameters.type.isNullOrEmpty() -> PlsIndexUtil.createNameTypeKey(queryParameters.name, queryParameters.type)
            !queryParameters.name.isNullOrEmpty() -> PlsIndexUtil.createNameKey(queryParameters.name)
            !queryParameters.type.isNullOrEmpty() -> PlsIndexUtil.createTypeKey(queryParameters.type)
            else -> PlsIndexUtil.createAllKey()
        }
    }

    private fun processInfo(
        queryParameters: ParadoxDefinitionSearch.SearchParameters,
        file: VirtualFile,
        info: ParadoxDefinitionIndexInfo,
        consumer: Processor<in ParadoxDefinitionIndexInfo>
    ): Boolean {
        if (queryParameters.name != null && queryParameters.name != info.name) return true
        if (queryParameters.type != null && queryParameters.type != info.type) return true
        info.bind(file, queryParameters.project)
        return consumer.process(info)
    }

    private fun matchesName(valueName: String, name: String?, ignoreCase: Boolean): Boolean {
        return name == null || valueName.equals(name, ignoreCase)
    }

    private fun matchesType(valueType: String, type: String?): Boolean {
        return type == null || valueType == type
    }

    private fun matchesSubtypes(info: ParadoxDefinitionIndexInfo, subtypes: List<String>?): Boolean {
        if (subtypes.isNullOrEmpty()) return true
        val indexedSubtypes = info.subtypes
        if (indexedSubtypes != null) return indexedSubtypes.containsAll(subtypes)
        val element = info.element ?: return false
        val resolvedSubtypes = ParadoxDefinitionManager.getSubtypes(element) ?: return false
        return resolvedSubtypes.containsAll(subtypes)
    }
}

