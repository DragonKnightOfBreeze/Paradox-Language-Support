package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.index.ParadoxDefinitionIndex
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.index.PlsIndexUtil
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.selector.getConstraint
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.model.constraints.ParadoxIndexConstraint
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

        val name = queryParameters.name
        val typeExpression = queryParameters.typeExpression?.let { ParadoxDefinitionTypeExpression.resolve(it) }
        val constraint0 = queryParameters.selector.getConstraint()

        val constraint = constraint0.optimized(typeExpression)
        val r = processQueryForDefinitions(name, typeExpression, queryParameters, scope, constraint, consumer)
        if (!r) return
    }

    private fun ParadoxIndexConstraint<ParadoxDefinitionIndexInfo>?.optimized(typeExpression: ParadoxDefinitionTypeExpression?): ParadoxIndexConstraint<ParadoxDefinitionIndexInfo>? {
        // 简化：仅在指定类型且存在对应约束时自动选用
        if (this != null) return this
        if (typeExpression == null) return null
        return ParadoxDefinitionIndexConstraint.get(typeExpression.type)
    }

    private fun processQueryForDefinitions(
        name: String?,
        typeExpression: ParadoxDefinitionTypeExpression?,
        queryParameters: ParadoxDefinitionSearch.SearchParameters,
        scope: GlobalSearchScope,
        constraint: ParadoxIndexConstraint<ParadoxDefinitionIndexInfo>?,
        processor: Processor<in ParadoxDefinitionIndexInfo>
    ): Boolean {
        if (queryParameters.forFile) {
            val r = processQueryForFileDefinitions(name, typeExpression, queryParameters, scope, constraint) { processor.process(it) }
            if (!r) return false
        }
        return processQueryForPropertyDefinitions(name, typeExpression, queryParameters, scope, constraint) { processor.process(it) }
    }

    private fun processQueryForFileDefinitions(
        name: String?,
        typeExpression: ParadoxDefinitionTypeExpression?,
        queryParameters: ParadoxDefinitionSearch.SearchParameters,
        scope: GlobalSearchScope,
        constraint: ParadoxIndexConstraint<ParadoxDefinitionIndexInfo>?,
        processor: Processor<ParadoxDefinitionIndexInfo>
    ): Boolean {
        ProgressManager.checkCanceled()
        val project = queryParameters.project
        val ignoreCase = constraint?.ignoreCase == true
        val finalName = if (ignoreCase) name?.lowercase() else name
        val actualKey = createActualKey(finalName, typeExpression?.type)
        val keys = setOf(actualKey)

        return FileBasedIndex.getInstance().processFilesContainingAnyKey(PlsIndexKeys.FileDefinition, keys, scope, null, null) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxAnalysisManager.getFileInfo(file) ?: return@p true
            if (queryParameters.gameType != null && selectGameType(file) != queryParameters.gameType) return@p true

            val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.FileDefinition, file, project)
            val data = fileData[actualKey] ?: return@p true
            if (!matchesName(data.name, finalName, ignoreCase)) return@p true
            if (!matchesType(data.type, typeExpression?.type)) return@p true

            val source = ParadoxDefinitionSource.File
            val info = ParadoxDefinitionIndexInfo(source, data.name, data.type, data.subtypes, data.typeKey, -1, data.gameType)
            info.bind(file, project)
            if (!matchesSubtypes(info, typeExpression?.subtypes)) return@p true
            processor.process(info)
        }
    }

    private fun processQueryForPropertyDefinitions(
        name: String?,
        typeExpression: ParadoxDefinitionTypeExpression?,
        queryParameters: ParadoxDefinitionSearch.SearchParameters,
        scope: GlobalSearchScope,
        constraint: ParadoxIndexConstraint<ParadoxDefinitionIndexInfo>?,
        processor: Processor<in ParadoxDefinitionIndexInfo>
    ): Boolean {
        ProgressManager.checkCanceled()
        val project = queryParameters.project
        val ignoreCase = constraint?.ignoreCase == true
        val finalName = if (ignoreCase) name?.lowercase() else name
        val actualKey = createActualKey(finalName, typeExpression?.type)
        val keys = buildSet {
            add(actualKey)
            add(PlsIndexUtil.createLazyKey())
        }
        val r = PlsIndexService.processAllFileData(ParadoxDefinitionIndex::class.java, keys, project, scope, queryParameters.gameType) p@{ file, fileData ->
            val infos = fileData[actualKey].orEmpty()
            infos.forEach { info ->
                ProgressManager.checkCanceled()
                if (!matchesName(info.name, finalName, ignoreCase)) return@forEach
                if (!matchesType(info.type, typeExpression?.type)) return@forEach
                val bound = info.copy(source = ParadoxDefinitionSource.Property)
                bound.bind(file, project)
                if (!matchesSubtypes(bound, typeExpression?.subtypes)) return@forEach
                processor.process(bound)
            }
            true
        }
        if (!r) return false

        if (constraint != null && constraint.inferred) {
            return processQueryForPropertyDefinitions(name, typeExpression, queryParameters, scope, null, processor)
        }

        return true
    }

    private fun createActualKey(name: String?, type: String?): String {
        return when {
            !name.isNullOrEmpty() && !type.isNullOrEmpty() -> PlsIndexUtil.createNameTypeKey(name, type)
            !name.isNullOrEmpty() -> PlsIndexUtil.createNameKey(name)
            !type.isNullOrEmpty() -> PlsIndexUtil.createTypeKey(type)
            else -> PlsIndexUtil.createAllKey()
        }
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
