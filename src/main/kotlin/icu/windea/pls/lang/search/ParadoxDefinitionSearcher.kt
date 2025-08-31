package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.swappedTypes
import icu.windea.pls.config.configGroup.types
import icu.windea.pls.core.processAllElements
import icu.windea.pls.core.processAllElementsByKeys
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.lang.search.selector.getConstraint
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.constraints.ParadoxIndexConstraint
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 定义的查询器。
 */
class ParadoxDefinitionSearcher : QueryExecutorBase<ParadoxScriptDefinitionElement, ParadoxDefinitionSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDefinitionSearch.SearchParameters, consumer: Processor<in ParadoxScriptDefinitionElement>) {
        //#141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsCoreManager.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        if (queryParameters.project.isDefault) return
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val name = queryParameters.name
        val typeExpression = queryParameters.typeExpression?.let { ParadoxDefinitionTypeExpression.resolve(it) }
        val project = queryParameters.project
        val gameType = queryParameters.selector.gameType ?: return
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val constraint = queryParameters.selector.getConstraint()

        val r = processQueryForDefinitions(name, typeExpression, configGroup, scope, constraint, consumer)
        if (!r) return

        // process swapped types
        if (typeExpression != null) {
            configGroup.swappedTypes.values.forEach f@{ swappedTypeConfig ->
                val baseType = swappedTypeConfig.baseType ?: return@f
                val baseTypeExpression = ParadoxDefinitionTypeExpression.resolve(baseType)
                if (typeExpression.matches(baseTypeExpression)) {
                    ProgressManager.checkCanceled()
                    val swappedTypeExpression = ParadoxDefinitionTypeExpression.resolve(swappedTypeConfig.name)
                    processQueryForDefinitions(name, swappedTypeExpression, configGroup, scope, constraint, consumer)
                }
            }
        }
    }

    private fun processQueryForDefinitions(
        name: String?,
        typeExpression: ParadoxDefinitionTypeExpression?,
        configGroup: CwtConfigGroup,
        scope: GlobalSearchScope,
        constraint: ParadoxIndexConstraint<ParadoxScriptDefinitionElement>?,
        processor: Processor<in ParadoxScriptDefinitionElement>
    ): Boolean {
        if (forFile(typeExpression, configGroup)) {
            return processQueryForFileDefinitions(name, typeExpression, configGroup.project, scope, constraint) { processor.process(it) }
        }
        return processQueryForStubDefinitions(name, typeExpression, configGroup.project, scope, constraint) { processor.process(it) }
    }

    private fun forFile(typeExpression: ParadoxDefinitionTypeExpression?, configGroup: CwtConfigGroup): Boolean {
        if (typeExpression?.text == "") return false
        return typeExpression == null || configGroup.types.get(typeExpression.type)?.typePerFile == true
    }

    private fun processQueryForFileDefinitions(
        name: String?,
        typeExpression: ParadoxDefinitionTypeExpression?,
        project: Project,
        scope: GlobalSearchScope,
        constraint: ParadoxIndexConstraint<ParadoxScriptDefinitionElement>?,
        processor: Processor<ParadoxScriptFile>
    ): Boolean {
        val ignoreCase = constraint?.ignoreCase == true
        return FileTypeIndex.processFiles(ParadoxScriptFileType, p@{
            ProgressManager.checkCanceled()
            val file = it.toPsiFile(project) ?: return@p true
            if (file !is ParadoxScriptFile) return@p true
            val definitionInfo = file.definitionInfo ?: return@p true
            if (name != null && !definitionInfo.name.equals(name, ignoreCase)) return@p true
            if (typeExpression != null && definitionInfo.type != typeExpression.type) return@p true
            if (typeExpression != null && typeExpression.subtypes.isNotEmpty() && !definitionInfo.subtypes.containsAll(typeExpression.subtypes)) return@p true
            processor.process(file)
        }, scope)
    }

    private fun processQueryForStubDefinitions(
        name: String?,
        typeExpression: ParadoxDefinitionTypeExpression?,
        project: Project,
        scope: GlobalSearchScope,
        constraint: ParadoxIndexConstraint<ParadoxScriptDefinitionElement>?,
        processor: Processor<in ParadoxScriptDefinitionElement>
    ): Boolean {
        val indexKey = constraint?.indexKey ?: ParadoxIndexKeys.DefinitionName
        val ignoreCase = constraint?.ignoreCase == true
        val finalName = if (ignoreCase) name?.lowercase() else name
        val r = if (typeExpression == null) {
            if (finalName == null) {
                indexKey.processAllElementsByKeys(project, scope) { _, element -> processor.process(element) }
            } else {
                indexKey.processAllElements(finalName, project, scope) { element -> processor.process(element) }
            }
        } else {
            if (finalName == null) {
                ParadoxIndexKeys.DefinitionType.processAllElements(typeExpression.type, project, scope) p@{ element ->
                    if (typeExpression.subtypes.isNotEmpty() && !matchesSubtypes(element, typeExpression.subtypes)) return@p true
                    processor.process(element)
                }
            } else {
                indexKey.processAllElements(finalName, project, scope) p@{ element ->
                    if (!matchesType(element, typeExpression.type)) return@p true
                    if (typeExpression.subtypes.isNotEmpty() && !matchesSubtypes(element, typeExpression.subtypes)) return@p true
                    processor.process(element)
                }
            }
        }
        if (!r) return false

        // fallback for inferred constraints
        if (constraint != null && constraint.inferred) {
            return processQueryForStubDefinitions(name, typeExpression, project, scope, null, processor)
        }

        return true
    }

    private fun matchesType(element: ParadoxScriptDefinitionElement, type: String): Boolean {
        return ParadoxDefinitionManager.getType(element) == type
    }

    private fun matchesSubtypes(element: ParadoxScriptDefinitionElement, subtypes: List<String>): Boolean {
        return ParadoxDefinitionManager.getSubtypes(element)?.containsAll(subtypes) == true
    }
}
