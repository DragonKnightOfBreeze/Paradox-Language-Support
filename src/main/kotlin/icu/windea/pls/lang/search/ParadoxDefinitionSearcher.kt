package icu.windea.pls.lang.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

/**
 * 定义的查询器。
 */
class ParadoxDefinitionSearcher : QueryExecutorBase<ParadoxScriptDefinitionElement, ParadoxDefinitionSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDefinitionSearch.SearchParameters, consumer: Processor<in ParadoxScriptDefinitionElement>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if(SearchScope.isEmptyScope(scope)) return
        val name = queryParameters.name
        val typeExpression = queryParameters.typeExpression?.let { ParadoxDefinitionTypeExpression.resolve(it) }
        val project = queryParameters.project
        val gameType = queryParameters.selector.gameType ?: return
        val configGroup = getConfigGroup(project, gameType)
        
        processQueryForFileDefinitions(name, typeExpression, project, scope, configGroup) { consumer.process(it) }
        processQueryForStubDefinitions(name, typeExpression, project, scope) { consumer.process(it) }
        
        if(typeExpression != null) {
            //如果存在切换类型，也要查找对应的切换类型的定义
            configGroup.swappedTypes.values.forEach f@{ swappedTypeConfig ->
                val baseType = swappedTypeConfig.baseType ?: return@f
                val baseTypeExpression = ParadoxDefinitionTypeExpression.resolve(baseType)
                if(typeExpression.matches(baseTypeExpression)) {
                    ProgressManager.checkCanceled()
                    val swappedTypeExpression = ParadoxDefinitionTypeExpression.resolve(swappedTypeConfig.name)
                    processQueryForFileDefinitions(name, swappedTypeExpression, project, scope, configGroup) { consumer.process(it) }
                    processQueryForStubDefinitions(name, swappedTypeExpression, project, scope) { consumer.process(it) }
                }
            }
        }
    }
    
    private fun processQueryForFileDefinitions(
        name: String?,
        typeExpression: ParadoxDefinitionTypeExpression?,
        project: Project,
        scope: GlobalSearchScope,
        configGroup: CwtConfigGroup,
        processor: Processor<ParadoxScriptFile>
    ) {
        if(typeExpression != null && configGroup.types.get(typeExpression.type)?.typePerFile != true) return
        FileTypeIndex.processFiles(ParadoxScriptFileType, p@{
            ProgressManager.checkCanceled()
            val file = it.toPsiFile(project) ?: return@p true
            if(file !is ParadoxScriptFile) return@p true
            val definitionInfo = file.definitionInfo ?: return@p true
            if(name != null && definitionInfo.name != name) return@p true
            if(typeExpression != null && definitionInfo.type != typeExpression.type) return@p true
            if(typeExpression != null && typeExpression.subtypes.isNotEmpty() && !definitionInfo.subtypes.containsAll(typeExpression.subtypes)) return@p true
            processor.process(file)
        }, scope)
    }
    
    private fun processQueryForStubDefinitions(
        name: String?,
        typeExpression: ParadoxDefinitionTypeExpression?,
        project: Project,
        scope: GlobalSearchScope,
        consumer: Processor<in ParadoxScriptDefinitionElement>
    ) {
        if(typeExpression == null) {
            if(name == null) {
                ParadoxDefinitionNameIndex.KEY.processAllElementsByKeys(project, scope) { _, it ->
                    consumer.process(it)
                }
            } else {
                ParadoxDefinitionNameIndex.KEY.processAllElements(name, project, scope) {
                    consumer.process(it)
                }
            }
        } else {
            if(name == null) {
                ParadoxDefinitionTypeIndex.KEY.processAllElements(typeExpression.type, project, scope) p@{
                    if(typeExpression.subtypes.isNotEmpty() && !matchesSubtypes(it, typeExpression.subtypes)) return@p true
                    consumer.process(it)
                }
            } else {
                ParadoxDefinitionNameIndex.KEY.processAllElements(name, project, scope) p@{
                    if(!matchesType(it, typeExpression.type)) return@p true
                    if(typeExpression.subtypes.isNotEmpty() && !matchesSubtypes(it, typeExpression.subtypes)) return@p true
                    consumer.process(it)
                }
            }
        }
    }
    
    private fun matchesName(element: ParadoxScriptDefinitionElement, name: String): Boolean {
        return ParadoxDefinitionHandler.getName(element) == name
    }
    
    private fun matchesType(element: ParadoxScriptDefinitionElement, type: String): Boolean {
        return ParadoxDefinitionHandler.getType(element) == type
    }
    
    private fun matchesSubtypes(element: ParadoxScriptDefinitionElement, subtypes: List<String>): Boolean {
        return ParadoxDefinitionHandler.getSubtypes(element)?.containsAll(subtypes) == true
    }
}
