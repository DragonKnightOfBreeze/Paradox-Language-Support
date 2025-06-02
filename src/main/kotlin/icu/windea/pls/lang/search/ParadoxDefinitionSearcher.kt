package icu.windea.pls.lang.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

/**
 * 定义的查询器。
 */
class ParadoxDefinitionSearcher : QueryExecutorBase<ParadoxScriptDefinitionElement, ParadoxDefinitionSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDefinitionSearch.SearchParameters, consumer: Processor<in ParadoxScriptDefinitionElement>) {
        //#141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsManager.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        if (queryParameters.project.isDefault) return
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val name = queryParameters.name
        val forFile = queryParameters.typeExpression != ""
        val typeExpression = queryParameters.typeExpression?.orNull()?.let { ParadoxDefinitionTypeExpression.resolve(it) }
        val project = queryParameters.project
        val gameType = queryParameters.selector.gameType ?: return
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val constraint = queryParameters.selector.getConstraint()

        if (forFile && forFile(typeExpression, configGroup)) {
            processQueryForFileDefinitions(name, typeExpression, project, scope, constraint) { consumer.process(it) }
        }
        processQueryForStubDefinitions(name, typeExpression, project, scope, constraint) { consumer.process(it) }

        if (typeExpression != null) {
            //如果存在切换类型，也要查找对应的切换类型的定义
            configGroup.swappedTypes.values.forEach f@{ swappedTypeConfig ->
                val baseType = swappedTypeConfig.baseType ?: return@f
                val baseTypeExpression = ParadoxDefinitionTypeExpression.resolve(baseType)
                if (typeExpression.matches(baseTypeExpression)) {
                    ProgressManager.checkCanceled()
                    val swappedTypeExpression = ParadoxDefinitionTypeExpression.resolve(swappedTypeConfig.name)
                    if (forFile && forFile(typeExpression, configGroup)) {
                        processQueryForFileDefinitions(name, swappedTypeExpression, project, scope, constraint) { consumer.process(it) }
                    }
                    processQueryForStubDefinitions(name, swappedTypeExpression, project, scope, constraint) { consumer.process(it) }
                }
            }
        }
    }

    private fun forFile(typeExpression: ParadoxDefinitionTypeExpression?, configGroup: CwtConfigGroup): Boolean {
        return typeExpression == null || configGroup.types.get(typeExpression.type)?.typePerFile == true
    }

    private fun processQueryForFileDefinitions(
        name: String?,
        typeExpression: ParadoxDefinitionTypeExpression?,
        project: Project,
        scope: GlobalSearchScope,
        constraint: ParadoxIndexConstraint<ParadoxScriptDefinitionElement>?,
        processor: Processor<ParadoxScriptFile>
    ) {
        val ignoreCase = constraint?.ignoreCase == true
        FileTypeIndex.processFiles(ParadoxScriptFileType, p@{
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
        consumer: Processor<in ParadoxScriptDefinitionElement>
    ) {
        val indexKey = constraint?.indexKey ?: ParadoxIndexManager.DefinitionNameKey
        val ignoreCase = constraint?.ignoreCase == true
        val finalName = if (ignoreCase) name?.lowercase() else name
        if (typeExpression == null) {
            if (finalName == null) {
                indexKey.processAllElementsByKeys(project, scope) { _, element -> consumer.process(element) }
            } else {
                indexKey.processAllElements(finalName, project, scope) { element -> consumer.process(element) }
            }
        } else {
            if (finalName == null) {
                ParadoxIndexManager.DefinitionTypeKey.processAllElements(typeExpression.type, project, scope) p@{ element ->
                    if (typeExpression.subtypes.isNotEmpty() && !matchesSubtypes(element, typeExpression.subtypes)) return@p true
                    consumer.process(element)
                }
            } else {
                indexKey.processAllElements(finalName, project, scope) p@{ element ->
                    if (!matchesType(element, typeExpression.type)) return@p true
                    if (typeExpression.subtypes.isNotEmpty() && !matchesSubtypes(element, typeExpression.subtypes)) return@p true
                    consumer.process(element)
                }
            }
        }
    }

    private fun matchesType(element: ParadoxScriptDefinitionElement, type: String): Boolean {
        return ParadoxDefinitionManager.getType(element) == type
    }

    private fun matchesSubtypes(element: ParadoxScriptDefinitionElement, subtypes: List<String>): Boolean {
        return ParadoxDefinitionManager.getSubtypes(element)?.containsAll(subtypes) == true
    }
}
