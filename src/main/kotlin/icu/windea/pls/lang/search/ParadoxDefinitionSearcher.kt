package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.letIf
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.index.ParadoxDefinitionIndex
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.index.PlsIndexUtil
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.selector.getConstraint
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.model.constraints.ParadoxDefinitionIndexConstraint
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

        val constraint = queryParameters.selector.getConstraint() as? ParadoxDefinitionIndexConstraint
        val keys = buildSet {
            add(createActualKey(queryParameters, constraint))
            add(PlsIndexUtil.createLazyKey())
        }
        PlsIndexService.processAllFileData(ParadoxDefinitionIndex::class.java, keys, project, scope, queryParameters.gameType) p@{ file, fileData ->
            val actualKey = createActualKey(queryParameters, constraint)
            val infos = fileData[actualKey].orEmpty()
            infos.process { info -> processInfo(queryParameters, file, info, constraint, consumer) }
        }
    }

    private fun createActualKey(queryParameters: ParadoxDefinitionSearch.SearchParameters, constraint: ParadoxDefinitionIndexConstraint?): String {
        val ignoreCase = constraint?.ignoreCase == true
        val name = queryParameters.name?.letIf(ignoreCase) { it.lowercase() }
        val type = queryParameters.type
        return when {
            !name.isNullOrEmpty() && !type.isNullOrEmpty() -> PlsIndexUtil.createNameTypeKey(name, type)
            !name.isNullOrEmpty() -> PlsIndexUtil.createNameKey(name)
            !type.isNullOrEmpty() -> PlsIndexUtil.createTypeKey(type)
            else -> PlsIndexUtil.createAllKey()
        }
    }

    private fun processInfo(
        queryParameters: ParadoxDefinitionSearch.SearchParameters,
        file: VirtualFile,
        info: ParadoxDefinitionIndexInfo,
        constraint: ParadoxDefinitionIndexConstraint?,
        consumer: Processor<in ParadoxDefinitionIndexInfo>
    ): Boolean {
        if (!matchesType(queryParameters, info)) return true
        if (!matchesName(queryParameters, info, constraint)) return true
        info.bind(file, queryParameters.project)
        if (!matchesSubtypes(queryParameters, info)) return true
        return consumer.process(info)
    }

    private fun matchesName(queryParameters: ParadoxDefinitionSearch.SearchParameters, info: ParadoxDefinitionIndexInfo, constraint: ParadoxDefinitionIndexConstraint?): Boolean {
        if (queryParameters.name == null) return true
        val ignoreCase = constraint?.ignoreCase == true
        return queryParameters.name.equals(info.name, ignoreCase)
    }

    private fun matchesType(queryParameters: ParadoxDefinitionSearch.SearchParameters, info: ParadoxDefinitionIndexInfo): Boolean {
        if (queryParameters.type == null) return true
        return queryParameters.type == info.type
    }

    private fun matchesSubtypes(queryParameters: ParadoxDefinitionSearch.SearchParameters, info: ParadoxDefinitionIndexInfo): Boolean {
        if (queryParameters.subtypes.isNullOrEmpty()) return true
        val fastSubtypes = info.fastSubtypes
        if (fastSubtypes.isNotEmpty() && fastSubtypes.containsAll(queryParameters.subtypes)) return true
        // 索引中的子类型可能不包含继承的子类型，需要通过 PSI 获取完整子类型进行二次检查
        val element = info.element ?: return false
        val subtypes = ParadoxDefinitionManager.getSubtypes(element) ?: return false
        return subtypes.containsAll(queryParameters.subtypes)
    }
}
