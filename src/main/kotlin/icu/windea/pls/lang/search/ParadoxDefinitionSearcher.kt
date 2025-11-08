package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.swappedTypes
import icu.windea.pls.config.configGroup.types
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.selector.getConstraint
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constraints.ParadoxIndexConstraint
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 定义的查询器。
 */
class ParadoxDefinitionSearcher : QueryExecutorBase<ParadoxScriptDefinitionElement, ParadoxDefinitionSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDefinitionSearch.SearchParameters, consumer: Processor<in ParadoxScriptDefinitionElement>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsStates.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType)
        if (SearchScope.isEmptyScope(scope)) return

        val name = queryParameters.name
        val typeExpression = queryParameters.typeExpression?.let { ParadoxDefinitionTypeExpression.resolve(it) }
        val gameType = queryParameters.selector.gameType
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val constraint0 = queryParameters.selector.getConstraint()

        val constraint = constraint0.optimized(typeExpression)
        val r = processQueryForDefinitions(name, typeExpression, configGroup, queryParameters, scope, constraint, consumer)
        if (!r) return

        // process swapped types
        if (typeExpression != null) {
            configGroup.swappedTypes.values.forEach f@{ swappedTypeConfig ->
                val baseType = swappedTypeConfig.baseType ?: return@f
                val baseTypeExpression = ParadoxDefinitionTypeExpression.resolve(baseType)
                if (typeExpression.matches(baseTypeExpression)) {
                    val swappedTypeExpression = ParadoxDefinitionTypeExpression.resolve(swappedTypeConfig.name)
                    val swappedConstraint = constraint0.optimized(swappedTypeExpression)
                    processQueryForDefinitions(name, swappedTypeExpression, configGroup, queryParameters, scope, swappedConstraint, consumer)
                }
            }
        }
    }

    private fun ParadoxIndexConstraint<ParadoxScriptDefinitionElement>?.optimized(typeExpression: ParadoxDefinitionTypeExpression?): ParadoxIndexConstraint<ParadoxScriptDefinitionElement>? {
        // 如果没有默认选用的约束，且存在指定的定义类型对应的约束，则自动选用
        if (this != null) return this
        if (typeExpression == null) return null
        return ParadoxIndexConstraint.Definition.get(typeExpression.type)
    }

    private fun processQueryForDefinitions(
        name: String?,
        typeExpression: ParadoxDefinitionTypeExpression?,
        configGroup: CwtConfigGroup,
        queryParameters: ParadoxDefinitionSearch.SearchParameters,
        scope: GlobalSearchScope,
        constraint: ParadoxIndexConstraint<ParadoxScriptDefinitionElement>?,
        processor: Processor<in ParadoxScriptDefinitionElement>
    ): Boolean {
        if (queryParameters.forFile && typePerFile(typeExpression, configGroup)) {
            return processQueryForFileDefinitions(name, typeExpression, queryParameters, scope, constraint) { processor.process(it) }
        }
        return processQueryForStubDefinitions(name, typeExpression, queryParameters, scope, constraint) { processor.process(it) }
    }

    private fun processQueryForFileDefinitions(
        name: String?,
        typeExpression: ParadoxDefinitionTypeExpression?,
        queryParameters: ParadoxDefinitionSearch.SearchParameters,
        scope: GlobalSearchScope,
        constraint: ParadoxIndexConstraint<ParadoxScriptDefinitionElement>?,
        processor: Processor<ParadoxScriptFile>
    ): Boolean {
        ProgressManager.checkCanceled()
        val project = queryParameters.project
        val ignoreCase = constraint?.ignoreCase == true
        return FileTypeIndex.processFiles(ParadoxScriptFileType, p@{
            ProgressManager.checkCanceled()
            val file = it.toPsiFile(project) ?: return@p true
            if (file !is ParadoxScriptFile) return@p true
            val definitionInfo = file.definitionInfo ?: return@p true
            if (!matchesName(definitionInfo, name, ignoreCase)) return@p true
            if (!matchesType(definitionInfo, typeExpression?.type)) return@p true
            if (!matchesSubtypes(definitionInfo, typeExpression?.subtypes)) return@p true
            processor.process(file)
        }, scope)
    }

    private fun processQueryForStubDefinitions(
        name: String?,
        typeExpression: ParadoxDefinitionTypeExpression?,
        queryParameters: ParadoxDefinitionSearch.SearchParameters,
        scope: GlobalSearchScope,
        constraint: ParadoxIndexConstraint<ParadoxScriptDefinitionElement>?,
        processor: Processor<in ParadoxScriptDefinitionElement>
    ): Boolean {
        ProgressManager.checkCanceled()
        val project = queryParameters.project
        val indexKey = constraint?.indexKey ?: PlsIndexKeys.DefinitionName
        val ignoreCase = constraint?.ignoreCase == true
        val finalName = if (ignoreCase) name?.lowercase() else name
        val r = if (typeExpression == null) {
            if (finalName == null) {
                PlsIndexService.processElementsByKeys(indexKey, project, scope) { _, element ->
                    processor.process(element)
                }
            } else {
                PlsIndexService.processElements(indexKey, finalName, project, scope) { element ->
                    processor.process(element)
                }
            }
        } else {
            if (finalName == null) {
                PlsIndexService.processElements(PlsIndexKeys.DefinitionType, typeExpression.type, project, scope) p@{ element ->
                    if (!matchesSubtypes(element, typeExpression.subtypes)) return@p true
                    processor.process(element)
                }
            } else {
                PlsIndexService.processElements(indexKey, finalName, project, scope) p@{ element ->
                    if (!matchesType(element, typeExpression.type)) return@p true
                    if (!matchesSubtypes(element, typeExpression.subtypes)) return@p true
                    processor.process(element)
                }
            }
        }
        if (!r) return false

        // fallback for inferred constraints
        if (constraint != null && constraint.inferred) {
            return processQueryForStubDefinitions(name, typeExpression, queryParameters, scope, null, processor)
        }

        return true
    }

    private fun typePerFile(typeExpression: ParadoxDefinitionTypeExpression?, configGroup: CwtConfigGroup): Boolean {
        return typeExpression == null || configGroup.types.get(typeExpression.type)?.typePerFile == true
    }

    private fun matchesName(definitionInfo: ParadoxDefinitionInfo, name: String?, ignoreCase: Boolean = false): Boolean {
        return name == null || definitionInfo.name.equals(name, ignoreCase)
    }

    private fun matchesType(definitionInfo: ParadoxDefinitionInfo, type: String?): Boolean {
        return type == null || definitionInfo.type == type
    }

    private fun matchesSubtypes(definitionInfo: ParadoxDefinitionInfo, subtypes: List<String>?): Boolean {
        return subtypes.isNullOrEmpty() || definitionInfo.subtypes.containsAll(subtypes)
    }

    // private fun matchesName(element: ParadoxScriptDefinitionElement, name: String?, ignoreCase: Boolean = false): Boolean {
    //     return name == null || ParadoxDefinitionManager.getName(element).equals(name, ignoreCase)
    // }

    private fun matchesType(element: ParadoxScriptDefinitionElement, type: String?): Boolean {
        return type == null || ParadoxDefinitionManager.getType(element) == type
    }

    private fun matchesSubtypes(element: ParadoxScriptDefinitionElement, subtypes: List<String>?): Boolean {
        return subtypes.isNullOrEmpty() || ParadoxDefinitionManager.getSubtypes(element)?.containsAll(subtypes) == true
    }
}
